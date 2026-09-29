package com.eve.healthcare.controller;

import com.eve.healthcare.BaseIntegrationTest;
import com.eve.healthcare.domain.model.Booking;
import com.eve.healthcare.domain.model.BookingStatus;
import com.eve.healthcare.domain.model.Payment;
import com.eve.healthcare.domain.model.PaymentStatus;
import com.eve.healthcare.domain.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WebhookControllerTest extends BaseIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    private Long createBookingAndPayment(String token, boolean paymentSuccess) throws Exception {
        Map<String, Object> bookingBody = Map.of(
                "centre_id", centre1.getId(),
                "test_id", test1.getId(),
                "appointment_datetime", OffsetDateTime.now().plusDays(4).toString()
        );

        String bResp = mockMvc.perform(post("/bookings/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingBody)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long bookingId = ((Number) objectMapper.readValue(bResp, Map.class).get("id")).longValue();

        paymentService.setSimulateSuccess(paymentSuccess);
        String pResp = mockMvc.perform(post("/payments/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("booking_id", bookingId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return ((Number) objectMapper.readValue(pResp, Map.class).get("id")).longValue();
    }

    @Test
    @DisplayName("POST /payments/webhook/ - idempotent processing prevents duplicate side-effects")
    public void testWebhookIdempotency() throws Exception {
        String token = registerUserAndGetToken("David", "david@example.com", "password123");
        Long paymentId = createBookingAndPayment(token, true);

        Map<String, Object> event = Map.of(
                "event_id", "evt_unique_12345",
                "payment_id", paymentId,
                "status", "SUCCESS"
        );

        // First delivery: processes event
        mockMvc.perform(post("/payments/webhook/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.event_id", is("evt_unique_12345")));

        long initialPaymentCount = paymentRepository.count();
        long initialBookingCount = bookingRepository.count();

        // Second delivery (duplicate event_id): must return duplicate status without duplicating records
        mockMvc.perform(post("/payments/webhook/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("duplicate")))
                .andExpect(jsonPath("$.message", is("Event already processed")));

        // Assert no duplicate payments or bookings were created
        assertEquals(initialPaymentCount, paymentRepository.count());
        assertEquals(initialBookingCount, bookingRepository.count());
    }

    @Test
    @DisplayName("POST /payments/webhook/ - SUCCESS webhook upgrades FAILED payment to SUCCESS and confirms booking")
    public void testWebhookUpgradesFailedPayment() throws Exception {
        String token = registerUserAndGetToken("David", "david@example.com", "password123");
        Long paymentId = createBookingAndPayment(token, false);

        // Payment initially FAILED
        Payment payment = paymentRepository.findByIdWithBooking(paymentId).orElseThrow();
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals(BookingStatus.FAILED, payment.getBooking().getStatus());

        // Webhook arrives with SUCCESS update
        Map<String, Object> event = Map.of(
                "event_id", "evt_upgrade_789",
                "payment_id", paymentId,
                "status", "SUCCESS"
        );

        mockMvc.perform(post("/payments/webhook/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("processed")));

        // Verify upgraded state in database
        Payment updatedPayment = paymentRepository.findByIdWithBooking(paymentId).orElseThrow();
        assertEquals(PaymentStatus.SUCCESS, updatedPayment.getStatus());
        assertEquals(BookingStatus.CONFIRMED, updatedPayment.getBooking().getStatus());
    }

    @Test
    @DisplayName("POST /payments/webhook/ - FAILED event arriving after SUCCESS is ignored (no downgrade)")
    public void testWebhookNoDowngrade() throws Exception {
        String token = registerUserAndGetToken("David", "david@example.com", "password123");
        Long paymentId = createBookingAndPayment(token, true);

        // Webhook arrives with late FAILED status
        Map<String, Object> event = Map.of(
                "event_id", "evt_late_fail_999",
                "payment_id", paymentId,
                "status", "FAILED"
        );

        mockMvc.perform(post("/payments/webhook/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ignored")))
                .andExpect(jsonPath("$.message", is("Payment already succeeded — event ignored")));

        // Payment and booking remain SUCCESS / CONFIRMED
        Payment payment = paymentRepository.findByIdWithBooking(paymentId).orElseThrow();
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals(BookingStatus.CONFIRMED, payment.getBooking().getStatus());
    }

    @Test
    @DisplayName("POST /payments/webhook/ - webhook for unknown payment ID returns 404")
    public void testWebhookUnknownPayment() throws Exception {
        Map<String, Object> event = Map.of(
                "event_id", "evt_unknown",
                "payment_id", 99999L,
                "status", "SUCCESS"
        );

        mockMvc.perform(post("/payments/webhook/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /payments/webhook/ - invalid status value returns 422")
    public void testWebhookInvalidStatus() throws Exception {
        Map<String, Object> event = Map.of(
                "event_id", "evt_invalid",
                "payment_id", 1L,
                "status", "PENDING_STATUS"
        );

        mockMvc.perform(post("/payments/webhook/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /payments/webhook/ - concurrent identical webhook deliveries result in exactly 1 processed event and 0 duplicate records")
    public void testConcurrentDuplicateWebhookDeliveries() throws Exception {
        String token = registerUserAndGetToken("David", "david@example.com", "password123");
        Long paymentId = createBookingAndPayment(token, false);

        String eventId = "evt_concurrent_" + System.currentTimeMillis();
        Map<String, Object> event = Map.of(
                "event_id", eventId,
                "payment_id", paymentId,
                "status", "SUCCESS"
        );

        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicInteger successCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger duplicateOrIgnoredCount = new java.util.concurrent.atomic.AtomicInteger(0);

        Runnable task = () -> {
            try {
                latch.await();
                var result = mockMvc.perform(post("/payments/webhook/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(event)))
                        .andReturn();

                if (result.getResponse().getStatus() == 200) {
                    String body = result.getResponse().getContentAsString();
                    if (body.contains("\"status\":\"processed\"")) {
                        successCount.incrementAndGet();
                    } else if (body.contains("\"status\":\"duplicate\"") || body.contains("\"status\":\"ignored\"")) {
                        duplicateOrIgnoredCount.incrementAndGet();
                    }
                }
            } catch (Exception e) {
                // Ignore thread interruptions
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();

        latch.countDown(); // Release both threads simultaneously
        t1.join();
        t2.join();

        // Exactly one webhook event should exist for this event_id in DB
        assertEquals(1, webhookEventRepository.count());
        Payment finalPayment = paymentRepository.findByIdWithBooking(paymentId).orElseThrow();
        assertEquals(PaymentStatus.SUCCESS, finalPayment.getStatus());
        assertEquals(BookingStatus.CONFIRMED, finalPayment.getBooking().getStatus());
    }
}
