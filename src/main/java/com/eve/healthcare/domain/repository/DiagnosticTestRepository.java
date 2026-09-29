package com.eve.healthcare.domain.repository;

import com.eve.healthcare.domain.model.DiagnosticTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiagnosticTestRepository extends JpaRepository<DiagnosticTest, Long> {
}
