package com.eve.healthcare.domain.service;

import com.eve.healthcare.common.exception.ResourceNotFoundException;
import com.eve.healthcare.domain.model.*;
import com.eve.healthcare.domain.repository.BookingRepository;
import com.eve.healthcare.domain.repository.PaymentRepository;
import com.eve.healthcare.domain.repository.WebhookEventRepository;
import com.eve.healthcare.web.dto.request.WebhookRequest;
import com.eve.healthcare.web.dto.response.WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final WebhookEventRepository webhookEventRepository;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public WebhookService(
            WebhookEventRepository webhookEventRepository,
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository) {
        this.webhookEventRepository = webhookEventRepository;
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public WebhookResponse processWebhook(WebhookRequest request) {
        // Fast-path idempotency check: check if event was already recorded
        if (webhookEventRepository.existsByEventId(request.getEventId())) {
            log.info("Duplicate webhook event received: {}", request.getEventId());
            return new WebhookResponse(request.getEventId(), "duplicate", "Event already processed");
        }

        // Locate payment and associated booking
        Payment payment = paymentRepository.findByIdWithBooking(request.getPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + request.getPaymentId()));

        Booking booking = payment.getBooking();

        // Parse target payment status
        PaymentStatus targetStatus = PaymentStatus.valueOf(request.getStatus().toUpperCase());

        WebhookProcessingStatus processingStatus;
        String message;

        // Apply state transition invariants
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            // SUCCESS is a terminal state — cannot be downgraded by delayed or out-of-order failure events
            processingStatus = WebhookProcessingStatus.IGNORED;
            message = "Payment already succeeded — event ignored";
        } else if (booking.getStatus() == BookingStatus.CANCELLED) {
            // Cancelled bookings do not transition state from webhooks
            processingStatus = WebhookProcessingStatus.IGNORED;
            message = "Booking is cancelled — event ignored";
        } else if (targetStatus == PaymentStatus.SUCCESS) {
            // Upgrade path: FAILED or initial payment -> SUCCESS
            payment.setStatus(PaymentStatus.SUCCESS);
            booking.setStatus(BookingStatus.CONFIRMED);
            paymentRepository.save(payment);
            bookingRepository.save(booking);
            processingStatus = WebhookProcessingStatus.PROCESSED;
            message = "Payment updated to SUCCESS, booking confirmed";
        } else {
            // FAILED status update
            payment.setStatus(PaymentStatus.FAILED);
            if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.CANCELLED) {
                booking.setStatus(BookingStatus.FAILED);
                bookingRepository.save(booking);
            }
            paymentRepository.save(payment);
            processingStatus = WebhookProcessingStatus.PROCESSED;
            message = "Payment updated to FAILED";
        }

        // Record the webhook event with database-level unique constraint protection
        try {
            WebhookEvent event = new WebhookEvent(
                    request.getEventId(),
                    payment,
                    "payment." + targetStatus.name().toLowerCase(),
                    processingStatus
            );
            event.setProcessedAt(OffsetDateTime.now());
            webhookEventRepository.saveAndFlush(event);
        } catch (DataIntegrityViolationException ex) {
            // Caught concurrent insertion of the same event_id
            log.warn("Concurrent duplicate webhook delivery caught by DB constraint: {}", request.getEventId());
            return new WebhookResponse(request.getEventId(), "duplicate", "Event already processed");
        }

        return new WebhookResponse(request.getEventId(), processingStatus.name().toLowerCase(), message);
    }
}
