package com.code.portfolio.project.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RiskCalculatorTest {

    private static final LocalDate INICIO = LocalDate.of(2025, 1, 1);

    private final RiskCalculator calculator = new RiskCalculator();

    @ParameterizedTest
    @CsvSource({
            "100000, BAIXO",
            "100001, MEDIO",
            "500000, MEDIO",
            "500001, ALTO",
            "750000, ALTO"
    })
    void avaliaRiscoPorOrcamento(BigDecimal orcamento, RiskLevel esperado) {
        // prazo de 2 meses (baixo) isola o criterio de orcamento
        assertThat(calculator.calcular(orcamento, INICIO, INICIO.plusMonths(2))).isEqualTo(esperado);
    }

    @ParameterizedTest
    @CsvSource({
            "3, BAIXO",
            "4, MEDIO",
            "6, MEDIO",
            "7, ALTO",
            "12, ALTO"
    })
    void avaliaRiscoPorPrazo(long meses, RiskLevel esperado) {
        // orcamento baixo isola o criterio de prazo
        assertThat(calculator.calcular(new BigDecimal("50000"), INICIO, INICIO.plusMonths(meses)))
                .isEqualTo(esperado);
    }

    @Test
    void riscoBaixoExigeOrcamentoEPrazoBaixos() {
        assertThat(calculator.calcular(new BigDecimal("100000"), INICIO, INICIO.plusMonths(3)))
                .isEqualTo(RiskLevel.BAIXO);
    }

    @Test
    void assumeOMaiorRiscoEntreOrcamentoEPrazo() {
        // orcamento alto + prazo baixo -> ALTO
        assertThat(calculator.calcular(new BigDecimal("600000"), INICIO, INICIO.plusMonths(1)))
                .isEqualTo(RiskLevel.ALTO);
        // orcamento baixo + prazo alto -> ALTO
        assertThat(calculator.calcular(new BigDecimal("1000"), INICIO, INICIO.plusMonths(10)))
                .isEqualTo(RiskLevel.ALTO);
        // orcamento baixo + prazo medio -> MEDIO
        assertThat(calculator.calcular(new BigDecimal("1000"), INICIO, INICIO.plusMonths(5)))
                .isEqualTo(RiskLevel.MEDIO);
    }
}
