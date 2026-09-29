package com.eve.healthcare.domain.service;

import com.eve.healthcare.common.exception.ConflictException;
import com.eve.healthcare.common.exception.ResourceNotFoundException;
import com.eve.healthcare.domain.model.Booking;
import com.eve.healthcare.domain.model.BookingStatus;
import com.eve.healthcare.domain.model.Payment;
import com.eve.healthcare.domain.model.PaymentStatus;
import com.eve.healthcare.domain.repository.BookingRepository;
import com.eve.healthcare.domain.repository.PaymentRepository;
import com.eve.healthcare.web.dto.request.PaymentCreateRequest;
import com.eve.healthcare.web.dto.response.PaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Value("${app.payment.simulate-success:#{null}}")
    private Boolean simulateSuccess;

    public PaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    public void setSimulateSuccess(Boolean simulateSuccess) {
        this.simulateSuccess = simulateSuccess;
    }

    @Transactional
    public PaymentResponse createPayment(Long userId, PaymentCreateRequest request) {
        // Use pessimistic lock on the booking to prevent race conditions during concurrent payment attempts
        Booking booking = bookingRepository.findByIdForUpdate(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + request.getBookingId()));

        if (!booking.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Booking not found with ID: " + request.getBookingId());
        }

        // Validate booking state
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new ConflictException("Booking is already confirmed — payment was already successful");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("Cannot pay for a cancelled booking");
        }
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.FAILED) {
            throw new ConflictException("Booking with status " + booking.getStatus() + " is not eligible for payment");
        }

        // Ensure no successful payment exists
        Optional<Payment> existingSuccess = paymentRepository.findByBookingIdAndStatus(booking.getId(), PaymentStatus.SUCCESS);
        if (existingSuccess.isPresent()) {
            throw new ConflictException("A successful payment already exists for this booking");
        }

        // Determine simulation outcome
        PaymentStatus paymentStatus = determineOutcome();
        String providerReference = "pay_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        Payment payment = new Payment(
                booking,
                booking.getAmount(),
                paymentStatus,
                providerReference
        );
        Payment savedPayment = paymentRepository.save(payment);

        // Update booking state atomically
        if (paymentStatus == PaymentStatus.SUCCESS) {
            booking.setStatus(BookingStatus.CONFIRMED);
        } else {
            booking.setStatus(BookingStatus.FAILED);
        }
        bookingRepository.save(booking);

        return PaymentResponse.fromPayment(savedPayment);
    }

    private PaymentStatus determineOutcome() {
        if (simulateSuccess != null) {
            return simulateSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        }
        // Default deterministic/simulation rule: 80% success
        return Math.random() < 0.8 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
    }
}
