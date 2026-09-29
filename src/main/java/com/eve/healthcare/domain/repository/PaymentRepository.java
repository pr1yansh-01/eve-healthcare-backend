package com.eve.healthcare.domain.repository;

import com.eve.healthcare.domain.model.Payment;
import com.eve.healthcare.domain.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findByBookingIdAndStatus(Long bookingId, PaymentStatus status);

    Optional<Payment> findByProviderReference(String providerReference);

    @Query("SELECT p FROM Payment p JOIN FETCH p.booking b JOIN FETCH b.user WHERE p.id = :id")
    Optional<Payment> findByIdWithBooking(@Param("id") Long id);
}
