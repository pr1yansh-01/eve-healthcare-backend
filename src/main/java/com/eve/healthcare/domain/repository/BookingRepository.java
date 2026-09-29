package com.eve.healthcare.domain.repository;

import com.eve.healthcare.domain.model.Booking;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.centreTest ct JOIN FETCH ct.centre JOIN FETCH ct.test WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Booking> findByUserIdWithDetails(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.centreTest ct JOIN FETCH ct.centre JOIN FETCH ct.test WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.centreTest ct JOIN FETCH ct.centre JOIN FETCH ct.test WHERE b.id = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") Long id);
}
