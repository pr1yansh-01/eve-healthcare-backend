package com.eve.healthcare.controller;

import com.eve.healthcare.BaseIntegrationTest;
import com.eve.healthcare.domain.model.Booking;
import com.eve.healthcare.domain.model.BookingStatus;
import com.eve.healthcare.domain.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PaymentControllerTest extends BaseIntegrationTest {

    @Autowired
    private PaymentService paymentService;

    private Long createBooking(String token) throws Exception {
        Map<String, Object> body = Map.of(
                "centre_id", centre1.getId(),
                "test_id", test1.getId(),
                "appointment_datetime", OffsetDateTime.now().plusDays(3).toString()
        );

        String response = mockMvc.perform(post("/bookings/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return ((Number) objectMapper.readValue(response, Map.class).get("id")).longValue();
    }

    @Test
    @DisplayName("POST /payments/ - payment success creates payment record and confirms booking")
    public void testPaymentSuccess() throws Exception {
        paymentService.setSimulateSuccess(true);
        String token = registerUserAndGetToken("Bob", "bob@example.com", "password123");
        Long bookingId = createBooking(token);

        Map<String, Object> payRequest = Map.of("booking_id", bookingId);

        mockMvc.perform(post("/payments/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.booking_id", is(bookingId.intValue())))
                .andExpect(jsonPath("$.amount", is(500.00)))
                .andExpect(jsonPath("$.provider_reference", notNullValue()));

        // Verify booking transitioned to CONFIRMED
        mockMvc.perform(get("/bookings/" + bookingId + "/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMED")));
    }

    @Test
    @DisplayName("POST /payments/ - payment failure marks booking as FAILED and allows retry")
    public void testPaymentFailureAndRetry() throws Exception {
        String token = registerUserAndGetToken("Bob", "bob@example.com", "password123");
        Long bookingId = createBooking(token);

        Map<String, Object> payRequest = Map.of("booking_id", bookingId);

        // 1. First attempt fails
        paymentService.setSimulateSuccess(false);
        mockMvc.perform(post("/payments/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("FAILED")));

        mockMvc.perform(get("/bookings/" + bookingId + "/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("FAILED")));

        // 2. Retry attempt succeeds
        paymentService.setSimulateSuccess(true);
        mockMvc.perform(post("/payments/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("SUCCESS")));

        mockMvc.perform(get("/bookings/" + bookingId + "/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CONFIRMED")));
    }

    @Test
    @DisplayName("POST /payments/ - payment for already CONFIRMED booking returns 409 Conflict")
    public void testPaymentForConfirmedBookingFails() throws Exception {
        paymentService.setSimulateSuccess(true);
        String token = registerUserAndGetToken("Bob", "bob@example.com", "password123");
        Long bookingId = createBooking(token);

        Map<String, Object> payRequest = Map.of("booking_id", bookingId);

        // Initial payment succeeds
        mockMvc.perform(post("/payments/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payRequest)))
                .andExpect(status().isCreated());

        // Duplicate payment attempt returns 409 Conflict
        mockMvc.perform(post("/payments/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /payments/ - payment for CANCELLED booking returns 409 Conflict")
    public void testPaymentForCancelledBookingFails() throws Exception {
        String token = registerUserAndGetToken("Bob", "bob@example.com", "password123");
        Long bookingId = createBooking(token);

        // Cancel booking
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        Map<String, Object> payRequest = Map.of("booking_id", bookingId);

        mockMvc.perform(post("/payments/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /payments/ - paying for other user's booking returns 404")
    public void testPaymentForOtherUserBooking() throws Exception {
        String tokenA = registerUserAndGetToken("User A", "usera@example.com", "password123");
        String tokenB = registerUserAndGetToken("User B", "userb@example.com", "password123");
        Long bookingId = createBooking(tokenA);

        Map<String, Object> payRequest = Map.of("booking_id", bookingId);

        mockMvc.perform(post("/payments/")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payRequest)))
                .andExpect(status().isNotFound());
    }
}
