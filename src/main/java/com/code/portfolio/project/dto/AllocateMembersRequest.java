package com.code.portfolio.project.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Solicitacao de alocacao de um ou mais membros a um projeto. */
public record AllocateMembersRequest(
        @NotEmpty List<@NotNull Long> memberIds) {
}
