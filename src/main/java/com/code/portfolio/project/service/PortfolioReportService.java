package com.code.portfolio.project.service;

import com.code.portfolio.project.domain.Project;
import com.code.portfolio.project.domain.ProjectStatus;
import com.code.portfolio.project.dto.PortfolioReportResponse;
import com.code.portfolio.project.repository.ProjectMemberRepository;
import com.code.portfolio.project.repository.ProjectRepository;
import com.code.portfolio.project.repository.StatusAggregation;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gera o relatorio resumido do portfolio. */
@Service
public class PortfolioReportService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public PortfolioReportService(
            ProjectRepository projectRepository, ProjectMemberRepository projectMemberRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional(readOnly = true)
    public PortfolioReportResponse generate() {
        Map<ProjectStatus, Long> countByStatus = new EnumMap<>(ProjectStatus.class);
        Map<ProjectStatus, BigDecimal> budgetByStatus = new EnumMap<>(ProjectStatus.class);
        for (StatusAggregation aggregation : projectRepository.aggregateByStatus()) {
            countByStatus.put(aggregation.getStatus(), aggregation.getQuantidade());
            budgetByStatus.put(aggregation.getStatus(), aggregation.getTotalOrcado());
        }

        return new PortfolioReportResponse(
                countByStatus,
                budgetByStatus,
                averageDurationOfClosedProjects(),
                projectMemberRepository.countDistinctMembers());
    }

    /** Media de duracao (em dias) dos projetos encerrados; {@code null} quando nao ha encerrados. */
    private Double averageDurationOfClosedProjects() {
        List<Project> closed = projectRepository.findByStatus(ProjectStatus.ENCERRADO);
        OptionalDouble average = closed.stream()
                .filter(project -> project.getActualEndDate() != null)
                .mapToLong(project -> ChronoUnit.DAYS.between(project.getStartDate(), project.getActualEndDate()))
                .average();
        return average.isPresent() ? average.getAsDouble() : null;
    }
}
