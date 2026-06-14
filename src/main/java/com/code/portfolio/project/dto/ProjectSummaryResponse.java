package com.code.portfolio.project.dto;

import com.code.portfolio.project.domain.ProjectStatus;
import com.code.portfolio.project.domain.RiskLevel;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Representacao resumida de um projeto, usada nas listagens paginadas. */
public record ProjectSummaryResponse(
        Long id,
        String name,
        ProjectStatus status,
        RiskLevel risk,
        BigDecimal totalBudget,
        LocalDate startDate,
        LocalDate expectedEndDate,
        Long managerId) {
}
