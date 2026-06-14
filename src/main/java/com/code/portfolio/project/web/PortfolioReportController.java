package com.code.portfolio.project.web;

import com.code.portfolio.project.dto.PortfolioReportResponse;
import com.code.portfolio.project.service.PortfolioReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Relatorios", description = "Relatorios do portfolio de projetos")
public class PortfolioReportController {

    private final PortfolioReportService service;

    public PortfolioReportController(PortfolioReportService service) {
        this.service = service;
    }

    @GetMapping("/portfolio")
    @Operation(summary = "Relatorio resumido: projetos e orcamento por status, "
            + "media de duracao dos encerrados e total de membros unicos")
    public PortfolioReportResponse portfolio() {
        return service.generate();
    }
}
