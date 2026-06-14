package com.code.portfolio.project.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Dados de entrada para criacao/atualizacao de um projeto.
 * Regras entre campos (ex.: previsao de termino posterior ao inicio) e a existencia
 * do gerente sao validadas na camada de servico.
 */
public record ProjectRequest(

        @NotBlank
        @Size(max = 150)
        String name,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate expectedEndDate,

        LocalDate actualEndDate,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        @Digits(integer = 13, fraction = 2)
        BigDecimal totalBudget,

        String description,

        @NotNull
        Long managerId) {
}
