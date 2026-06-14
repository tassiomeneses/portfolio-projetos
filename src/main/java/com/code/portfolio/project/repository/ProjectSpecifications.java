package com.code.portfolio.project.repository;

import com.code.portfolio.project.domain.Project;
import com.code.portfolio.project.domain.ProjectStatus;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/** Filtros dinamicos para a listagem de projetos. */
public final class ProjectSpecifications {

    private ProjectSpecifications() {
    }

    public static Specification<Project> withFilters(String name, ProjectStatus status, Long managerId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + name.toLowerCase(Locale.ROOT) + "%"));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (managerId != null) {
                predicates.add(cb.equal(root.get("managerId"), managerId));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
