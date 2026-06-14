package com.code.portfolio.project.repository;

import com.code.portfolio.project.domain.ProjectStatus;
import java.math.BigDecimal;

/** Projecao usada para agregar projetos por status (contagem e total orcado). */
public interface StatusAggregation {

    ProjectStatus getStatus();

    long getQuantidade();

    BigDecimal getTotalOrcado();
}
