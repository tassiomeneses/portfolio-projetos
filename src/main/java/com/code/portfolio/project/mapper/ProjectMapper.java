package com.code.portfolio.project.mapper;

import com.code.portfolio.project.domain.Project;
import com.code.portfolio.project.domain.ProjectMember;
import com.code.portfolio.project.domain.RiskCalculator;
import com.code.portfolio.project.domain.RiskLevel;
import com.code.portfolio.project.dto.ProjectRequest;
import com.code.portfolio.project.dto.ProjectResponse;
import com.code.portfolio.project.dto.ProjectSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    private final RiskCalculator riskCalculator;

    public ProjectMapper(RiskCalculator riskCalculator) {
        this.riskCalculator = riskCalculator;
    }

    public Project toEntity(ProjectRequest request) {
        Project project = new Project();
        applyEditableFields(project, request);
        return project;
    }

    public void updateEntity(Project project, ProjectRequest request) {
        applyEditableFields(project, request);
    }

    private void applyEditableFields(Project project, ProjectRequest request) {
        project.setName(request.name());
        project.setStartDate(request.startDate());
        project.setExpectedEndDate(request.expectedEndDate());
        project.setActualEndDate(request.actualEndDate());
        project.setTotalBudget(request.totalBudget());
        project.setDescription(request.description());
        project.setManagerId(request.managerId());
    }

    public ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getStartDate(),
                project.getExpectedEndDate(),
                project.getActualEndDate(),
                project.getTotalBudget(),
                project.getDescription(),
                project.getManagerId(),
                project.getStatus(),
                risk(project),
                project.getMembers().stream().map(ProjectMember::getMemberId).toList(),
                project.getCreatedAt(),
                project.getUpdatedAt());
    }

    public ProjectSummaryResponse toSummary(Project project) {
        return new ProjectSummaryResponse(
                project.getId(),
                project.getName(),
                project.getStatus(),
                risk(project),
                project.getTotalBudget(),
                project.getStartDate(),
                project.getExpectedEndDate(),
                project.getManagerId());
    }

    private RiskLevel risk(Project project) {
        return riskCalculator.calcular(
                project.getTotalBudget(), project.getStartDate(), project.getExpectedEndDate());
    }
}
