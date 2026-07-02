package com.code.portfolio.project.service;

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
import com.code.portfolio.project.repository.ProjectSpecifications;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso e regras de negocio dos projetos. Membros sao sempre consultados
 * na API externa via {@link MemberClient}; o dominio guarda apenas os identificadores.
 */
@Service
public class ProjectService {

    static final int MIN_MEMBROS = 1;
    static final int MAX_MEMBROS = 10;
    static final int MAX_PROJETOS_ATIVOS_POR_MEMBRO = 3;

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMapper mapper;
    private final MemberClient memberClient;
    private final RiskCalculator riskCalculator;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            ProjectMapper mapper,
            MemberClient memberClient,
            RiskCalculator riskCalculator) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.mapper = mapper;
        this.memberClient = memberClient;
        this.riskCalculator = riskCalculator;
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        validateDates(request);
        validateManager(request.managerId());
        Project project = mapper.toEntity(request); // status inicial (EM_ANALISE) definido na persistencia
        return mapper.toResponse(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public ProjectResponse findById(Long id) {
        return mapper.toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<ProjectSummaryResponse> list(
            String name, ProjectStatus status, Long managerId, RiskLevel risk, Pageable pageable) {
        if (risk != null) {
            return listByCalculatedRisk(name, status, managerId, risk, pageable);
        }
        return projectRepository
                .findAll(ProjectSpecifications.withFilters(name, status, managerId), pageable)
                .map(mapper::toSummary);
    }

    private Page<ProjectSummaryResponse> listByCalculatedRisk(
            String name, ProjectStatus status, Long managerId, RiskLevel risk, Pageable pageable) {
        List<Project> filtered = projectRepository
                .findAll(ProjectSpecifications.withFilters(name, status, managerId), pageable.getSort())
                .stream()
                .filter(project -> risk(project) == risk)
                .toList();

        if (pageable.isUnpaged()) {
            return new PageImpl<>(filtered.stream().map(mapper::toSummary).toList());
        }

        int start = (int) Math.min(pageable.getOffset(), filtered.size());
        int end = (int) Math.min((long) start + pageable.getPageSize(), filtered.size());
        List<ProjectSummaryResponse> content = filtered.subList(start, end).stream()
                .map(mapper::toSummary)
                .toList();
        return new PageImpl<>(content, pageable, filtered.size());
    }

    private RiskLevel risk(Project project) {
        return riskCalculator.calcular(
                project.getTotalBudget(), project.getStartDate(), project.getExpectedEndDate());
    }

    @Transactional
    public ProjectResponse update(Long id, ProjectRequest request) {
        Project project = getOrThrow(id);
        validateDates(request);
        validateManager(request.managerId());
        mapper.updateEntity(project, request);
        return mapper.toResponse(project);
    }

    @Transactional
    public void delete(Long id) {
        Project project = getOrThrow(id);
        if (project.getStatus().bloqueiaExclusao()) {
            throw new BusinessRuleException(
                    "Projetos no status %s nao podem ser excluidos.".formatted(project.getStatus()));
        }
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectResponse changeStatus(Long id, ProjectStatus newStatus) {
        Project project = getOrThrow(id);
        ProjectStatus current = project.getStatus();

        if (current == newStatus) {
            throw new BusinessRuleException("O projeto ja esta no status %s.".formatted(newStatus));
        }
        if (!current.podeTransicionarPara(newStatus)) {
            throw new BusinessRuleException(
                    "Transicao invalida: de %s para %s. Permitidos a partir de %s: %s."
                            .formatted(current, newStatus, current, current.proximosPermitidos()));
        }
        if (newStatus == ProjectStatus.INICIADO && project.memberCount() < MIN_MEMBROS) {
            throw new BusinessRuleException(
                    "O projeto precisa de ao menos %d membro alocado para ser iniciado.".formatted(MIN_MEMBROS));
        }
        if (newStatus == ProjectStatus.ENCERRADO && project.getActualEndDate() == null) {
            project.setActualEndDate(LocalDate.now());
        }
        project.setStatus(newStatus);
        return mapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse allocateMembers(Long id, List<Long> memberIds) {
        Project project = getOrThrow(id);
        for (Long memberId : memberIds.stream().distinct().toList()) {
            allocateSingle(project, memberId);
        }
        return mapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse deallocateMember(Long id, Long memberId) {
        Project project = getOrThrow(id);
        if (!project.removeMemberById(memberId)) {
            throw new ResourceNotFoundException(
                    "Membro %d nao esta alocado no projeto %d.".formatted(memberId, id));
        }
        return mapper.toResponse(project);
    }

    private void allocateSingle(Project project, Long memberId) {
        if (project.hasMember(memberId)) {
            throw new BusinessRuleException("O membro %d ja esta alocado neste projeto.".formatted(memberId));
        }
        if (project.memberCount() >= MAX_MEMBROS) {
            throw new BusinessRuleException(
                    "Um projeto pode ter no maximo %d membros alocados.".formatted(MAX_MEMBROS));
        }
        Member member = memberClient.findById(memberId)
                .orElseThrow(() -> new BusinessRuleException(
                        "Membro %d nao encontrado na API de membros.".formatted(memberId)));
        if (!member.isFuncionario()) {
            throw new BusinessRuleException(
                    "Apenas membros com atribuicao 'funcionario' podem ser alocados. Membro %d possui '%s'."
                            .formatted(memberId, member.attribution()));
        }
        long active = projectMemberRepository.countActiveAllocationsByMember(memberId, ProjectStatus.finais());
        if (active >= MAX_PROJETOS_ATIVOS_POR_MEMBRO) {
            throw new BusinessRuleException(
                    "O membro %d ja esta alocado em %d projetos ativos (maximo %d)."
                            .formatted(memberId, active, MAX_PROJETOS_ATIVOS_POR_MEMBRO));
        }
        project.addMember(new ProjectMember(memberId));
    }

    private Project getOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto %d nao encontrado.".formatted(id)));
    }

    private void validateDates(ProjectRequest request) {
        if (!request.expectedEndDate().isAfter(request.startDate())) {
            throw new BusinessRuleException("A previsao de termino deve ser posterior a data de inicio.");
        }
        if (request.actualEndDate() != null && request.actualEndDate().isBefore(request.startDate())) {
            throw new BusinessRuleException("A data real de termino nao pode ser anterior a data de inicio.");
        }
    }

    private void validateManager(Long managerId) {
        if (memberClient.findById(managerId).isEmpty()) {
            throw new BusinessRuleException("Gerente %d nao encontrado na API de membros.".formatted(managerId));
        }
    }
}
