package com.code.portfolio.project.dto;

import com.code.portfolio.project.domain.ProjectStatus;
import com.code.portfolio.project.domain.RiskLevel;
import java.math.BigDecimal;
import java.util.Map;

/** Relatorio resumido do portfolio de projetos. */
public record PortfolioReportResponse(
        Map<ProjectStatus, Long> quantidadePorStatus,
        Map<ProjectStatus, BigDecimal> totalOrcadoPorStatus,
        Map<RiskLevel, Long> quantidadePorRisco,
        Double mediaDuracaoDiasProjetosEncerrados,
        long totalMembrosUnicosAlocados) {
}
