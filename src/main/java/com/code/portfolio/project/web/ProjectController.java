package com.code.portfolio.project.web;

import com.code.portfolio.project.domain.ProjectStatus;
import com.code.portfolio.project.dto.AllocateMembersRequest;
import com.code.portfolio.project.dto.ChangeStatusRequest;
import com.code.portfolio.project.dto.ProjectRequest;
import com.code.portfolio.project.dto.ProjectResponse;
import com.code.portfolio.project.dto.ProjectSummaryResponse;
import com.code.portfolio.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projetos", description = "Gestao do portfolio de projetos")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cria um novo projeto (inicia no status EM_ANALISE)")
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectRequest request) {
        ProjectResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/projects/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um projeto pelo id")
    public ProjectResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping
    @Operation(summary = "Lista projetos com filtros (name, status, managerId) e paginacao")
    public Page<ProjectSummaryResponse> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) Long managerId,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return service.list(name, status, managerId, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de um projeto")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um projeto (bloqueado em INICIADO, EM_ANDAMENTO ou ENCERRADO)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Altera o status respeitando a sequencia logica (sem pular etapas)")
    public ProjectResponse changeStatus(
            @PathVariable Long id, @Valid @RequestBody ChangeStatusRequest request) {
        return service.changeStatus(id, request.status());
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Aloca membros: apenas 'funcionario', 1..10 por projeto, max 3 projetos ativos por membro")
    public ProjectResponse allocate(
            @PathVariable Long id, @Valid @RequestBody AllocateMembersRequest request) {
        return service.allocateMembers(id, request.memberIds());
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @Operation(summary = "Remove a alocacao de um membro do projeto")
    public ProjectResponse deallocate(@PathVariable Long id, @PathVariable Long memberId) {
        return service.deallocateMember(id, memberId);
    }
}
