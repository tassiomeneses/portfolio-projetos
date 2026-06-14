package com.code.portfolio.project.repository;

import com.code.portfolio.project.domain.ProjectMember;
import com.code.portfolio.project.domain.ProjectStatus;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    /** Total de membros distintos alocados em qualquer projeto. */
    @Query("select count(distinct pm.memberId) from ProjectMember pm")
    long countDistinctMembers();

    /** Quantidade de alocacoes ativas (projeto com status fora dos informados) de um membro. */
    @Query("""
            select count(pm) from ProjectMember pm
            where pm.memberId = :memberId
              and pm.project.status not in :statuses
            """)
    long countActiveAllocationsByMember(
            @Param("memberId") Long memberId,
            @Param("statuses") Collection<ProjectStatus> statuses);
}
