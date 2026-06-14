package com.code.portfolio.project.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ProjectStatusTest {

    @Test
    void permiteAvancarPassoAPassoAteEncerrado() {
        assertThat(ProjectStatus.EM_ANALISE.podeTransicionarPara(ProjectStatus.ANALISE_REALIZADA)).isTrue();
        assertThat(ProjectStatus.ANALISE_REALIZADA.podeTransicionarPara(ProjectStatus.ANALISE_APROVADA)).isTrue();
        assertThat(ProjectStatus.ANALISE_APROVADA.podeTransicionarPara(ProjectStatus.INICIADO)).isTrue();
        assertThat(ProjectStatus.INICIADO.podeTransicionarPara(ProjectStatus.PLANEJADO)).isTrue();
        assertThat(ProjectStatus.PLANEJADO.podeTransicionarPara(ProjectStatus.EM_ANDAMENTO)).isTrue();
        assertThat(ProjectStatus.EM_ANDAMENTO.podeTransicionarPara(ProjectStatus.ENCERRADO)).isTrue();
    }

    @Test
    void naoPermitePularEtapas() {
        assertThat(ProjectStatus.EM_ANALISE.podeTransicionarPara(ProjectStatus.ANALISE_APROVADA)).isFalse();
        assertThat(ProjectStatus.EM_ANALISE.podeTransicionarPara(ProjectStatus.INICIADO)).isFalse();
        assertThat(ProjectStatus.ANALISE_APROVADA.podeTransicionarPara(ProjectStatus.EM_ANDAMENTO)).isFalse();
    }

    @Test
    void naoPermiteRetroceder() {
        assertThat(ProjectStatus.ANALISE_REALIZADA.podeTransicionarPara(ProjectStatus.EM_ANALISE)).isFalse();
        assertThat(ProjectStatus.INICIADO.podeTransicionarPara(ProjectStatus.ANALISE_APROVADA)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ProjectStatus.class,
            names = {"EM_ANALISE", "ANALISE_REALIZADA", "ANALISE_APROVADA", "INICIADO", "PLANEJADO", "EM_ANDAMENTO"})
    void permiteCancelarDeQualquerStatusNaoFinal(ProjectStatus status) {
        assertThat(status.podeTransicionarPara(ProjectStatus.CANCELADO)).isTrue();
    }

    @Test
    void statusFinaisNaoTransicionam() {
        for (ProjectStatus destino : ProjectStatus.values()) {
            assertThat(ProjectStatus.ENCERRADO.podeTransicionarPara(destino)).isFalse();
            assertThat(ProjectStatus.CANCELADO.podeTransicionarPara(destino)).isFalse();
        }
    }

    @Test
    void identificaStatusQueBloqueiamExclusao() {
        assertThat(ProjectStatus.INICIADO.bloqueiaExclusao()).isTrue();
        assertThat(ProjectStatus.EM_ANDAMENTO.bloqueiaExclusao()).isTrue();
        assertThat(ProjectStatus.ENCERRADO.bloqueiaExclusao()).isTrue();
        assertThat(ProjectStatus.EM_ANALISE.bloqueiaExclusao()).isFalse();
        assertThat(ProjectStatus.CANCELADO.bloqueiaExclusao()).isFalse();
    }

    @Test
    void identificaStatusFinais() {
        assertThat(ProjectStatus.ENCERRADO.isFinal()).isTrue();
        assertThat(ProjectStatus.CANCELADO.isFinal()).isTrue();
        assertThat(ProjectStatus.EM_ANDAMENTO.isFinal()).isFalse();
    }
}
