package com.eve.healthcare.domain.repository;

import com.eve.healthcare.domain.model.DiagnosticCentre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiagnosticCentreRepository extends JpaRepository<DiagnosticCentre, Long> {

    @Query("SELECT DISTINCT c FROM DiagnosticCentre c LEFT JOIN FETCH c.centreTests ct LEFT JOIN FETCH ct.test WHERE c.id = :id")
    Optional<DiagnosticCentre> findByIdWithTests(@Param("id") Long id);
}
