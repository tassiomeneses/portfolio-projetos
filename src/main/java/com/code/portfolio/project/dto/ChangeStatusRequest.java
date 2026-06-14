package com.code.portfolio.project.dto;

import com.code.portfolio.project.domain.ProjectStatus;
import jakarta.validation.constraints.NotNull;

/** Solicitacao de mudanca de status de um projeto. */
public record ChangeStatusRequest(
        @NotNull ProjectStatus status) {
}
