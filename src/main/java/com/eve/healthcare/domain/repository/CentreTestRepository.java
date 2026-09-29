package com.eve.healthcare.domain.repository;

import com.eve.healthcare.domain.model.CentreTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CentreTestRepository extends JpaRepository<CentreTest, Long> {

    @Query("SELECT ct FROM CentreTest ct JOIN FETCH ct.test WHERE ct.centre.id = :centreId")
    List<CentreTest> findByCentreIdWithTest(@Param("centreId") Long centreId);

    @Query("SELECT ct FROM CentreTest ct JOIN FETCH ct.centre JOIN FETCH ct.test WHERE ct.centre.id = :centreId AND ct.test.id = :testId")
    Optional<CentreTest> findByCentreIdAndTestId(@Param("centreId") Long centreId, @Param("testId") Long testId);
}
