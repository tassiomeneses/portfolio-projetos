package com.code.portfolio.membersprovider;

import com.code.portfolio.membersprovider.dto.MemberResponse;
import jakarta.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

/**
 * Implementacao em memoria da API externa (mockada) de membros. Simula um servico
 * de terceiros: mantem seu proprio armazenamento, independente do banco da aplicacao.
 *
 * <p>Alguns membros sao pre-carregados para facilitar os testes manuais/Postman.
 */
@Service
public class MemberProviderService {

    private final ConcurrentMap<Long, MemberResponse> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @PostConstruct
    void seed() {
        create("Ana Pereira", "gerente");      // id 1 - gerente
        create("Bruno Silva", "funcionário");  // id 2 - funcionario
        create("Carla Souza", "funcionário");  // id 3 - funcionario
        create("Diego Lima", "funcionário");   // id 4 - funcionario
        create("Eduarda Reis", "estagiário");  // id 5 - nao pode ser alocado
    }

    public MemberResponse create(String name, String attribution) {
        long id = sequence.incrementAndGet();
        MemberResponse member = new MemberResponse(id, name, attribution);
        store.put(id, member);
        return member;
    }

    public Optional<MemberResponse> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<MemberResponse> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(MemberResponse::id))
                .toList();
    }
}
