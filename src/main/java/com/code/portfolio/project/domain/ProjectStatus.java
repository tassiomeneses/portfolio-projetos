package com.code.portfolio.project.domain;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Status possiveis de um projeto. Sao fixos (nao cadastraveis) e seguem uma
 * sequencia logica que nao pode pular etapas:
 *
 * <pre>
 * EM_ANALISE -> ANALISE_REALIZADA -> ANALISE_APROVADA -> INICIADO
 *            -> PLANEJADO -> EM_ANDAMENTO -> ENCERRADO
 * </pre>
 *
 * CANCELADO e uma excecao: pode ser aplicado a partir de qualquer status nao final.
 */
public enum ProjectStatus {

    EM_ANALISE,
    ANALISE_REALIZADA,
    ANALISE_APROVADA,
    INICIADO,
    PLANEJADO,
    EM_ANDAMENTO,
    ENCERRADO,
    CANCELADO;

    /** Transicoes validas a partir de cada status. */
    private static final Map<ProjectStatus, Set<ProjectStatus>> TRANSICOES;

    static {
        Map<ProjectStatus, Set<ProjectStatus>> m = new EnumMap<>(ProjectStatus.class);
        m.put(EM_ANALISE, EnumSet.of(ANALISE_REALIZADA, CANCELADO));
        m.put(ANALISE_REALIZADA, EnumSet.of(ANALISE_APROVADA, CANCELADO));
        m.put(ANALISE_APROVADA, EnumSet.of(INICIADO, CANCELADO));
        m.put(INICIADO, EnumSet.of(PLANEJADO, CANCELADO));
        m.put(PLANEJADO, EnumSet.of(EM_ANDAMENTO, CANCELADO));
        m.put(EM_ANDAMENTO, EnumSet.of(ENCERRADO, CANCELADO));
        m.put(ENCERRADO, EnumSet.noneOf(ProjectStatus.class));
        m.put(CANCELADO, EnumSet.noneOf(ProjectStatus.class));
        TRANSICOES = Collections.unmodifiableMap(m);
    }

    /** Status que, uma vez atingidos, impedem a exclusao do projeto. */
    private static final Set<ProjectStatus> BLOQUEIAM_EXCLUSAO =
            EnumSet.of(INICIADO, EM_ANDAMENTO, ENCERRADO);

    /** Status finais: nao contam como alocacao ativa de um membro. */
    private static final Set<ProjectStatus> FINAIS = EnumSet.of(ENCERRADO, CANCELADO);

    /** Indica se a transicao deste status para {@code destino} e permitida. */
    public boolean podeTransicionarPara(ProjectStatus destino) {
        return destino != null && TRANSICOES.getOrDefault(this, Set.of()).contains(destino);
    }

    /** Conjunto de status para os quais este status pode transicionar. */
    public Set<ProjectStatus> proximosPermitidos() {
        return TRANSICOES.getOrDefault(this, Set.of());
    }

    /** Indica se este status impede a exclusao do projeto. */
    public boolean bloqueiaExclusao() {
        return BLOQUEIAM_EXCLUSAO.contains(this);
    }

    /** Indica se este e um status final (encerrado ou cancelado). */
    public boolean isFinal() {
        return FINAIS.contains(this);
    }

    /** Conjunto imutavel dos status finais. */
    public static Set<ProjectStatus> finais() {
        return FINAIS;
    }
}
