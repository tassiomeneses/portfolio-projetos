package com.code.portfolio.project.dto;

import com.code.portfolio.project.domain.ProjectStatus;
import com.code.portfolio.project.domain.RiskLevel;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Representacao completa de um projeto. O risco e calculado dinamicamente. */
public record ProjectResponse(
        Long id,
        String name,
        LocalDate startDate,
        LocalDate expectedEndDate,
        LocalDate actualEndDate,
        BigDecimal totalBudget,
        String description,
        Long managerId,
        ProjectStatus status,
        RiskLevel risk,
        List<Long> memberIds,
        Instant createdAt,
        Instant updatedAt) {
}
