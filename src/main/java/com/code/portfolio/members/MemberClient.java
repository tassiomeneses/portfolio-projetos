package com.code.portfolio.members;

import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP para a API externa de membros. O dominio nunca acessa membros
 * diretamente no banco: sempre consulta esse provedor externo.
 */
@Component
public class MemberClient {

    private final RestClient restClient;

    public MemberClient(RestClient membersRestClient) {
        this.restClient = membersRestClient;
    }

    /** Consulta um membro pelo id; retorna vazio quando o provedor responde 404. */
    public Optional<Member> findById(Long id) {
        try {
            Member member = restClient.get()
                    .uri("/members/{id}", id)
                    .retrieve()
                    .body(Member.class);
            return Optional.ofNullable(member);
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        }
    }
}
