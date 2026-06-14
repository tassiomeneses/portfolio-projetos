package com.code.portfolio.project.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

/**
 * Calcula dinamicamente a classificacao de risco de um projeto combinando dois
 * criterios independentes (orcamento e prazo) e assumindo o de maior severidade:
 *
 * <ul>
 *   <li>Baixo: orcamento ate R$ 100.000 <b>e</b> prazo de ate 3 meses;</li>
 *   <li>Medio: orcamento entre R$ 100.001 e R$ 500.000 <b>ou</b> prazo de 3 a 6 meses;</li>
 *   <li>Alto: orcamento acima de R$ 500.000 <b>ou</b> prazo superior a 6 meses.</li>
 * </ul>
 *
 * O prazo e medido em meses completos entre o inicio e a previsao de termino.
 */
@Component
public class RiskCalculator {

    private static final BigDecimal LIMITE_ORCAMENTO_BAIXO = new BigDecimal("100000");
    private static final BigDecimal LIMITE_ORCAMENTO_MEDIO = new BigDecimal("500000");
    private static final long LIMITE_PRAZO_BAIXO_MESES = 3;
    private static final long LIMITE_PRAZO_MEDIO_MESES = 6;

    public RiskLevel calcular(BigDecimal orcamento, LocalDate inicio, LocalDate previsaoTermino) {
        RiskLevel porOrcamento = avaliarOrcamento(orcamento);
        RiskLevel porPrazo = avaliarPrazo(inicio, previsaoTermino);
        return maisSevero(porOrcamento, porPrazo);
    }

    private RiskLevel avaliarOrcamento(BigDecimal orcamento) {
        if (orcamento.compareTo(LIMITE_ORCAMENTO_BAIXO) <= 0) {
            return RiskLevel.BAIXO;
        }
        if (orcamento.compareTo(LIMITE_ORCAMENTO_MEDIO) <= 0) {
            return RiskLevel.MEDIO;
        }
        return RiskLevel.ALTO;
    }

    private RiskLevel avaliarPrazo(LocalDate inicio, LocalDate previsaoTermino) {
        long meses = ChronoUnit.MONTHS.between(inicio, previsaoTermino);
        if (meses <= LIMITE_PRAZO_BAIXO_MESES) {
            return RiskLevel.BAIXO;
        }
        if (meses <= LIMITE_PRAZO_MEDIO_MESES) {
            return RiskLevel.MEDIO;
        }
        return RiskLevel.ALTO;
    }

    private RiskLevel maisSevero(RiskLevel a, RiskLevel b) {
        return a.ordinal() >= b.ordinal() ? a : b;
    }
}
