package com.code.portfolio.project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * Alocacao de um membro (gerenciado pela API externa) em um projeto.
 * Guarda apenas o identificador do membro, ja que membros nao sao persistidos localmente.
 */
@Entity
@Table(
        name = "project_member",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_project_member",
                columnNames = {"project_id", "member_id"}))
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "allocated_at", nullable = false)
    private Instant allocatedAt;

    protected ProjectMember() {
        // exigido pelo JPA
    }

    public ProjectMember(Long memberId) {
        this.memberId = memberId;
    }

    @PrePersist
    void onCreate() {
        this.allocatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Long getMemberId() {
        return memberId;
    }

    public Instant getAllocatedAt() {
        return allocatedAt;
    }
}
