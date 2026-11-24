package com.dan.backend.repository;

import com.dan.backend.entity.Placement;
import com.dan.backend.enums.PlacementStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlacementRepository extends JpaRepository<Placement, Long> {

    List<Placement> findByStatusOrderByCreatedAtDesc(PlacementStatus status);

    List<Placement> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    long countByStatus(PlacementStatus status);

    @Query("select p from Placement p where p.status = :status and ("
            + "lower(p.title) like lower(concat('%', :q, '%')) "
            + "or lower(p.location) like lower(concat('%', :q, '%')) "
            + "or lower(p.requiredSkills) like lower(concat('%', :q, '%'))) "
            + "order by p.createdAt desc")
    List<Placement> search(@Param("status") PlacementStatus status, @Param("q") String q);
}
