package com.code.portfolio.membersprovider.dto;

import jakarta.validation.constraints.NotBlank;

/** Dados para criacao de um membro na API externa (mockada). */
public record CreateMemberRequest(
        @NotBlank String name,
        @NotBlank String attribution) {
}
