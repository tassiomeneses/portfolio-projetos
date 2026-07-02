package com.code.portfolio.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.code.portfolio.project.domain.Project;
import com.code.portfolio.project.domain.ProjectStatus;
import com.code.portfolio.project.domain.RiskCalculator;
import com.code.portfolio.project.domain.RiskLevel;
import com.code.portfolio.project.dto.PortfolioReportResponse;
import com.code.portfolio.project.repository.ProjectMemberRepository;
import com.code.portfolio.project.repository.ProjectRepository;
import com.code.portfolio.project.repository.StatusAggregation;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PortfolioReportServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMemberRepository projectMemberRepository;

    private PortfolioReportService service;

    @BeforeEach
    void setUp() {
        service = new PortfolioReportService(projectRepository, projectMemberRepository, new RiskCalculator());
    }

    @Test
    void gerarAgregaQuantidadeETotalPorStatusEMembrosUnicos() {
        when(projectRepository.aggregateByStatus()).thenReturn(List.of(
                aggregation(ProjectStatus.EM_ANALISE, 2, "150000"),
                aggregation(ProjectStatus.ENCERRADO, 1, "300000")));
        when(projectRepository.findAll()).thenReturn(List.of());
        when(projectRepository.findByStatus(ProjectStatus.ENCERRADO)).thenReturn(List.of());
        when(projectMemberRepository.countDistinctMembers()).thenReturn(4L);

        PortfolioReportResponse report = service.generate();

        assertThat(report.quantidadePorStatus()).containsEntry(ProjectStatus.EM_ANALISE, 2L);
        assertThat(report.totalOrcadoPorStatus()).containsEntry(ProjectStatus.ENCERRADO, new BigDecimal("300000"));
        assertThat(report.quantidadePorRisco()).isEmpty();
        assertThat(report.totalMembrosUnicosAlocados()).isEqualTo(4L);
        assertThat(report.mediaDuracaoDiasProjetosEncerrados()).isNull();
    }

    @Test
    void gerarCalculaMediaDeDuracaoDosProjetosEncerrados() {
        when(projectRepository.aggregateByStatus()).thenReturn(List.of());
        when(projectRepository.findAll()).thenReturn(List.of());
        when(projectRepository.findByStatus(ProjectStatus.ENCERRADO)).thenReturn(List.of(
                closed(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 11)),   // 10 dias
                closed(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 21))));  // 20 dias
        when(projectMemberRepository.countDistinctMembers()).thenReturn(0L);

        PortfolioReportResponse report = service.generate();

        assertThat(report.mediaDuracaoDiasProjetosEncerrados()).isEqualTo(15.0);
    }

    @Test
    void gerarAgregaQuantidadePorRiscoCalculado() {
        when(projectRepository.aggregateByStatus()).thenReturn(List.of());
        when(projectRepository.findAll()).thenReturn(List.of(
                project("50000", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 1)),
                project("250000", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 5, 1)),
                project("750000", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 1))));
        when(projectRepository.findByStatus(ProjectStatus.ENCERRADO)).thenReturn(List.of());
        when(projectMemberRepository.countDistinctMembers()).thenReturn(0L);

        PortfolioReportResponse report = service.generate();

        assertThat(report.quantidadePorRisco()).containsEntry(RiskLevel.BAIXO, 1L);
        assertThat(report.quantidadePorRisco()).containsEntry(RiskLevel.MEDIO, 1L);
        assertThat(report.quantidadePorRisco()).containsEntry(RiskLevel.ALTO, 1L);
    }

    private StatusAggregation aggregation(ProjectStatus status, long quantidade, String total) {
        return new StatusAggregation() {
            @Override
            public ProjectStatus getStatus() {
                return status;
            }

            @Override
            public long getQuantidade() {
                return quantidade;
            }

            @Override
            public BigDecimal getTotalOrcado() {
                return new BigDecimal(total);
            }
        };
    }

    private Project closed(LocalDate start, LocalDate actualEnd) {
        Project project = new Project();
        project.setName("Encerrado");
        project.setStartDate(start);
        project.setExpectedEndDate(start.plusMonths(1));
        project.setActualEndDate(actualEnd);
        project.setTotalBudget(new BigDecimal("1000"));
        project.setManagerId(1L);
        project.setStatus(ProjectStatus.ENCERRADO);
        return project;
    }

    private Project project(String totalBudget, LocalDate start, LocalDate expectedEnd) {
        Project project = new Project();
        project.setName("Projeto");
        project.setStartDate(start);
        project.setExpectedEndDate(expectedEnd);
        project.setTotalBudget(new BigDecimal(totalBudget));
        project.setManagerId(1L);
        project.setStatus(ProjectStatus.EM_ANALISE);
        return project;
    }
}
