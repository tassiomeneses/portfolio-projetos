package com.code.portfolio.project.repository;

import com.code.portfolio.project.domain.Project;
import com.code.portfolio.project.domain.ProjectStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository
        extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    @Query("""
            select p.status as status,
                   count(p) as quantidade,
                   coalesce(sum(p.totalBudget), 0) as totalOrcado
            from Project p
            group by p.status
            """)
    List<StatusAggregation> aggregateByStatus();

    List<Project> findByStatus(ProjectStatus status);
}
