package com.code.portfolio.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.code.portfolio.common.exception.BusinessRuleException;
import com.code.portfolio.common.exception.ResourceNotFoundException;
import com.code.portfolio.members.Member;
import com.code.portfolio.members.MemberClient;
import com.code.portfolio.project.domain.Project;
import com.code.portfolio.project.domain.ProjectMember;
import com.code.portfolio.project.domain.ProjectStatus;
import com.code.portfolio.project.domain.RiskCalculator;
import com.code.portfolio.project.domain.RiskLevel;
import com.code.portfolio.project.dto.ProjectRequest;
import com.code.portfolio.project.dto.ProjectResponse;
import com.code.portfolio.project.dto.ProjectSummaryResponse;
import com.code.portfolio.project.mapper.ProjectMapper;
import com.code.portfolio.project.repository.ProjectMemberRepository;
import com.code.portfolio.project.repository.ProjectRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMemberRepository projectMemberRepository;
    @Mock private MemberClient memberClient;

    private ProjectService service;

    @BeforeEach
    void setUp() {
        ProjectMapper mapper = new ProjectMapper(new RiskCalculator());
        service = new ProjectService(
                projectRepository, projectMemberRepository, mapper, memberClient, new RiskCalculator());
    }

    // ---------------- create ----------------

    @Test
    void create_persisteProjetoNoStatusInicial() {
        when(memberClient.findById(1L)).thenReturn(Optional.of(gerente(1L)));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponse response = service.create(request());

        assertThat(response.status()).isEqualTo(ProjectStatus.EM_ANALISE);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void create_falhaQuandoPrevisaoTerminoNaoEPosteriorAoInicio() {
        ProjectRequest invalido = new ProjectRequest("X", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 1),
                null, new BigDecimal("1000"), "d", 1L);

        assertThatThrownBy(() -> service.create(invalido)).isInstanceOf(BusinessRuleException.class);
        verify(projectRepository, never()).save(any());
    }

    @Test
    void create_falhaQuandoGerenteNaoExiste() {
        when(memberClient.findById(99L)).thenReturn(Optional.empty());
        ProjectRequest req = new ProjectRequest("X", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 1),
                null, new BigDecimal("1000"), "d", 99L);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Gerente");
    }

    // ---------------- delete ----------------

    @Test
    void delete_removeQuandoStatusPermite() {
        Project project = project(ProjectStatus.EM_ANALISE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        service.delete(1L);

        // cast para CrudRepository desambigua a sobrecarga delete(...) do Spring Data JPA 4
        verify((CrudRepository<Project, Long>) projectRepository).delete(project);
    }

    @Test
    void delete_bloqueadoQuandoStatusImpede() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(ProjectStatus.EM_ANDAMENTO)));

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(BusinessRuleException.class);
        verify((CrudRepository<Project, Long>) projectRepository, never()).delete(any());
    }

    @Test
    void delete_falhaQuandoProjetoNaoExiste() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------------- changeStatus ----------------

    @Test
    void changeStatus_avancaUmaEtapa() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(ProjectStatus.EM_ANALISE)));

        ProjectResponse response = service.changeStatus(1L, ProjectStatus.ANALISE_REALIZADA);

        assertThat(response.status()).isEqualTo(ProjectStatus.ANALISE_REALIZADA);
    }

    @Test
    void changeStatus_recusaPularEtapas() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(ProjectStatus.EM_ANALISE)));

        assertThatThrownBy(() -> service.changeStatus(1L, ProjectStatus.INICIADO))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void changeStatus_recusaMesmoStatus() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(ProjectStatus.EM_ANALISE)));

        assertThatThrownBy(() -> service.changeStatus(1L, ProjectStatus.EM_ANALISE))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void changeStatus_iniciarExigePeloMenosUmMembro() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(ProjectStatus.ANALISE_APROVADA)));

        assertThatThrownBy(() -> service.changeStatus(1L, ProjectStatus.INICIADO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("membro");
    }

    @Test
    void changeStatus_iniciarComMembroFunciona() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(ProjectStatus.ANALISE_APROVADA, 2L)));

        ProjectResponse response = service.changeStatus(1L, ProjectStatus.INICIADO);

        assertThat(response.status()).isEqualTo(ProjectStatus.INICIADO);
    }

    @Test
    void changeStatus_encerrarDefineDataRealDeTermino() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(ProjectStatus.EM_ANDAMENTO, 2L)));

        ProjectResponse response = service.changeStatus(1L, ProjectStatus.ENCERRADO);

        assertThat(response.status()).isEqualTo(ProjectStatus.ENCERRADO);
        assertThat(response.actualEndDate()).isNotNull();
    }

    // ---------------- allocateMembers ----------------

    @Test
    void allocate_adicionaFuncionario() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(ProjectStatus.EM_ANALISE)));
        when(memberClient.findById(2L)).thenReturn(Optional.of(funcionario(2L)));
        when(projectMemberRepository.countActiveAllocationsByMember(eq(2L), any())).thenReturn(0L);

        ProjectResponse response = service.allocateMembers(1L, List.of(2L));

        assertThat(response.memberIds()).containsExactly(2L);
    }

    @Test
    void allocate_recusaNaoFuncionario() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(ProjectStatus.EM_ANALISE)));
        when(memberClient.findById(5L)).thenReturn(Optional.of(new Member(5L, "Estagiario", "estagiário")));

        assertThatThrownBy(() -> service.allocateMembers(1L, List.of(5L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("funcionario");
    }

    @Test
    void allocate_recusaMembroInexistente() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(ProjectStatus.EM_ANALISE)));
        when(memberClient.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.allocateMembers(1L, List.of(77L)))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void allocate_recusaQuandoMembroEmTresProjetosAtivos() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(ProjectStatus.EM_ANALISE)));
        when(memberClient.findById(2L)).thenReturn(Optional.of(funcionario(2L)));
        when(projectMemberRepository.countActiveAllocationsByMember(eq(2L), any())).thenReturn(3L);

        assertThatThrownBy(() -> service.allocateMembers(1L, List.of(2L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ativos");
    }

    @Test
    void allocate_recusaQuandoExcedeMaximoDeMembros() {
        // projeto ja com 10 membros
        Project lotado = project(ProjectStatus.EM_ANALISE, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(lotado));

        assertThatThrownBy(() -> service.allocateMembers(1L, List.of(2L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("maximo");
    }

    @Test
    void allocate_recusaMembroJaAlocadoNoProjeto() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(ProjectStatus.EM_ANALISE, 2L)));

        assertThatThrownBy(() -> service.allocateMembers(1L, List.of(2L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ja esta alocado");
    }

    // ---------------- deallocate ----------------

    @Test
    void deallocate_removeMembro() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(ProjectStatus.EM_ANALISE, 2L)));

        ProjectResponse response = service.deallocateMember(1L, 2L);

        assertThat(response.memberIds()).isEmpty();
    }

    @Test
    void deallocate_falhaQuandoMembroNaoAlocado() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project(ProjectStatus.EM_ANALISE)));

        assertThatThrownBy(() -> service.deallocateMember(1L, 2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------------- findById ----------------

    @Test
    void findById_falhaQuandoNaoExiste() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------------- list ----------------

    @Test
    void list_filtraPorRiscoCalculado() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("id"));
        when(projectRepository.findAll(anyProjectSpecification(), eq(pageable.getSort()))).thenReturn(List.of(
                listProject("Baixo", "50000", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 1), 1L),
                listProject("Medio", "250000", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 5, 1), 1L),
                listProject("Alto", "750000", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 1), 1L)));

        Page<ProjectSummaryResponse> response = service.list(null, null, null, RiskLevel.ALTO, pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent()).extracting(ProjectSummaryResponse::name).containsExactly("Alto");
        assertThat(response.getContent()).allSatisfy(project -> assertThat(project.risk()).isEqualTo(RiskLevel.ALTO));
    }

    @Test
    void list_combinaFiltroDeGerenteComRisco() {
        Pageable pageable = PageRequest.of(0, 20);
        when(projectRepository.findAll(anyProjectSpecification(), eq(pageable.getSort()))).thenReturn(List.of(
                listProject("Alto gerente 1", "750000", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 1), 1L),
                listProject("Medio gerente 1", "250000", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 5, 1), 1L)));

        Page<ProjectSummaryResponse> response = service.list(null, null, 1L, RiskLevel.ALTO, pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent()).extracting(ProjectSummaryResponse::name).containsExactly("Alto gerente 1");
        assertThat(response.getContent()).allSatisfy(project -> {
            assertThat(project.managerId()).isEqualTo(1L);
            assertThat(project.risk()).isEqualTo(RiskLevel.ALTO);
        });
    }

    @Test
    void list_semRiscoMantemFluxoPaginadoExistente() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("id"));
        Project project = listProject("Projeto existente", "50000", LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 3, 1), 1L);
        when(projectRepository.findAll(anyProjectSpecification(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(project), pageable, 1));

        Page<ProjectSummaryResponse> response = service.list(null, ProjectStatus.EM_ANALISE, 1L, null, pageable);

        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent()).extracting(ProjectSummaryResponse::name).containsExactly("Projeto existente");
        verify(projectRepository).findAll(anyProjectSpecification(), eq(pageable));
        verify(projectRepository, never()).findAll(anyProjectSpecification(), any(Sort.class));
    }

    @Test
    void list_filtraPorRiscoComMetadadosDePaginacaoCorretos() {
        Pageable pageable = PageRequest.of(1, 2, Sort.by("name"));
        when(projectRepository.findAll(anyProjectSpecification(), eq(pageable.getSort()))).thenReturn(List.of(
                listProject("Alto A", "750000", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 1), 1L),
                listProject("Baixo", "50000", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 1), 1L),
                listProject("Alto B", "800000", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 1), 1L),
                listProject("Alto C", "900000", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 1), 1L)));

        Page<ProjectSummaryResponse> response = service.list(null, null, null, RiskLevel.ALTO, pageable);

        assertThat(response.getNumber()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(2);
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(response.getContent()).extracting(ProjectSummaryResponse::name).containsExactly("Alto C");
    }

    // ---------------- helpers ----------------

    private ProjectRequest request() {
        return new ProjectRequest("Projeto X", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 1),
                null, new BigDecimal("50000"), "desc", 1L);
    }

    private Project project(ProjectStatus status, long... memberIds) {
        Project project = new Project();
        project.setName("Projeto X");
        project.setStartDate(LocalDate.of(2025, 1, 1));
        project.setExpectedEndDate(LocalDate.of(2025, 3, 1));
        project.setTotalBudget(new BigDecimal("50000"));
        project.setManagerId(1L);
        project.setStatus(status);
        for (long memberId : memberIds) {
            project.addMember(new ProjectMember(memberId));
        }
        return project;
    }

    private Project listProject(String name, String totalBudget, LocalDate startDate, LocalDate expectedEndDate,
            Long managerId) {
        Project project = new Project();
        project.setName(name);
        project.setStartDate(startDate);
        project.setExpectedEndDate(expectedEndDate);
        project.setTotalBudget(new BigDecimal(totalBudget));
        project.setManagerId(managerId);
        project.setStatus(ProjectStatus.EM_ANALISE);
        return project;
    }

    private Specification<Project> anyProjectSpecification() {
        return any();
    }

    private Member funcionario(Long id) {
        return new Member(id, "Funcionario " + id, "funcionário");
    }

    private Member gerente(Long id) {
        return new Member(id, "Gerente " + id, "gerente");
    }
}
