package com.code.portfolio.membersprovider;

import com.code.portfolio.membersprovider.dto.CreateMemberRequest;
import com.code.portfolio.membersprovider.dto.MemberResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API externa (mockada) de membros. Representa um servico de terceiros e, por isso,
 * fica fora da seguranca da aplicacao. O dominio de projetos a consome via {@code MemberClient}.
 */
@RestController
@RequestMapping("/api/members-provider/members")
@Tag(name = "Membros (API externa mockada)",
        description = "Servico externo simulado para criar e consultar membros.")
public class MembersProviderController {

    private final MemberProviderService service;

    public MembersProviderController(MemberProviderService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cria um membro no provedor externo")
    public ResponseEntity<MemberResponse> create(@Valid @RequestBody CreateMemberRequest request) {
        MemberResponse created = service.create(request.name(), request.attribution());
        return ResponseEntity
                .created(URI.create("/api/members-provider/members/" + created.id()))
                .body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um membro pelo id")
    public ResponseEntity<MemberResponse> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Lista os membros cadastrados no provedor externo")
    public List<MemberResponse> findAll() {
        return service.findAll();
    }
}
