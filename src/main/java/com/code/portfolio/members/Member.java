package com.code.portfolio.members;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Representacao de um membro retornado pela API externa de membros.
 * A atribuicao (cargo) define se o membro pode ser alocado a projetos.
 */
public record Member(Long id, String name, String attribution) {

    private static final String ATRIBUICAO_FUNCIONARIO = "funcionario";

    /** Apenas membros com atribuicao "funcionario" podem ser alocados a projetos. */
    public boolean isFuncionario() {
        return normalizar(attribution).equals(ATRIBUICAO_FUNCIONARIO);
    }

    private static String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.trim().toLowerCase(Locale.ROOT);
    }
}
