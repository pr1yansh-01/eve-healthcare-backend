package com.eve.healthcare.controller;

import com.eve.healthcare.BaseIntegrationTest;
import com.eve.healthcare.domain.model.Booking;
import com.eve.healthcare.domain.model.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BookingControllerTest extends BaseIntegrationTest {

    private String futureDateTime() {
        return OffsetDateTime.now().plusDays(5).toString();
    }

    @Test
    @DisplayName("POST /bookings/ - successfully creates booking with price snapshot and PENDING status")
    public void testCreateBookingSuccess() throws Exception {
        String token = registerUserAndGetToken("John", "john@example.com", "password123");

        Map<String, Object> body = Map.of(
                "centre_id", centre1.getId(),
                "test_id", test1.getId(),
                "appointment_datetime", futureDateTime()
        );

        mockMvc.perform(post("/bookings/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.centre_name", is("HealthFirst Diagnostics")))
                .andExpect(jsonPath("$.test_name", is("Complete Blood Count (CBC)")))
                .andExpect(jsonPath("$.amount", is(500.00)));
    }

    @Test
    @DisplayName("POST /bookings/ - snapshots price so subsequent centre price changes do not alter booking amount")
    public void testPriceSnapshotImmutability() throws Exception {
        String token = registerUserAndGetToken("John", "john@example.com", "password123");

        Map<String, Object> body = Map.of(
                "centre_id", centre1.getId(),
                "test_id", test1.getId(),
                "appointment_datetime", futureDateTime()
        );

        String response = mockMvc.perform(post("/bookings/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount", is(500.00)))
                .andReturn().getResponse().getContentAsString();

        Long bookingId = ((Number) objectMapper.readValue(response, Map.class).get("id")).longValue();

        // Admin updates the centre price
        ct1_1.setPrice(new BigDecimal("999.00"));
        centreTestRepository.save(ct1_1);

        // Verify the booked amount remains unchanged at 500.00
        mockMvc.perform(get("/bookings/" + bookingId + "/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(500.00)));
    }

    @Test
    @DisplayName("POST /bookings/ - non-existent centre returns 404 Not Found")
    public void testCreateBookingNonExistentCentre() throws Exception {
        String token = registerUserAndGetToken("John", "john@example.com", "password123");

        Map<String, Object> body = Map.of(
                "centre_id", 99999L,
                "test_id", test1.getId(),
                "appointment_datetime", futureDateTime()
        );

        mockMvc.perform(post("/bookings/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /bookings/ - test not offered by centre returns 404 Not Found")
    public void testCreateBookingTestNotOfferedByCentre() throws Exception {
        String token = registerUserAndGetToken("John", "john@example.com", "password123");

        // Centre 1 does not offer Test 5 (Liver Function Test)
        Map<String, Object> body = Map.of(
                "centre_id", centre1.getId(),
                "test_id", test5.getId(),
                "appointment_datetime", futureDateTime()
        );

        mockMvc.perform(post("/bookings/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /bookings/ - past appointment datetime returns 422 or 400")
    public void testCreateBookingPastDatetime() throws Exception {
        String token = registerUserAndGetToken("John", "john@example.com", "password123");

        Map<String, Object> body = Map.of(
                "centre_id", centre1.getId(),
                "test_id", test1.getId(),
                "appointment_datetime", OffsetDateTime.now().minusDays(1).toString()
        );

        mockMvc.perform(post("/bookings/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("GET /bookings/ - lists user's own bookings and preserves user isolation")
    public void testGetUserBookingsIsolation() throws Exception {
        String tokenA = registerUserAndGetToken("User A", "usera@example.com", "password123");
        String tokenB = registerUserAndGetToken("User B", "userb@example.com", "password123");

        // User A creates a booking
        Map<String, Object> body = Map.of(
                "centre_id", centre1.getId(),
                "test_id", test1.getId(),
                "appointment_datetime", futureDateTime()
        );

        mockMvc.perform(post("/bookings/")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        // User B views their bookings — should see 0
        mockMvc.perform(get("/bookings/")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET /bookings/{id}/ - other user accessing booking returns 404 (Privacy policy)")
    public void testGetOtherUserBooking() throws Exception {
        String tokenA = registerUserAndGetToken("User A", "usera@example.com", "password123");
        String tokenB = registerUserAndGetToken("User B", "userb@example.com", "password123");

        Map<String, Object> body = Map.of(
                "centre_id", centre1.getId(),
                "test_id", test1.getId(),
                "appointment_datetime", futureDateTime()
        );

        String response = mockMvc.perform(post("/bookings/")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long bookingId = ((Number) objectMapper.readValue(response, Map.class).get("id")).longValue();

        // User B attempts to access User A's booking
        mockMvc.perform(get("/bookings/" + bookingId + "/")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /bookings/{id}/cancel/ - cancels PENDING booking and repeated cancellation is idempotent")
    public void testCancelBookingIdempotency() throws Exception {
        String token = registerUserAndGetToken("John", "john@example.com", "password123");

        Map<String, Object> body = Map.of(
                "centre_id", centre1.getId(),
                "test_id", test1.getId(),
                "appointment_datetime", futureDateTime()
        );

        String response = mockMvc.perform(post("/bookings/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long bookingId = ((Number) objectMapper.readValue(response, Map.class).get("id")).longValue();

        // First cancellation
        mockMvc.perform(post("/bookings/" + bookingId + "/cancel/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        // Second cancellation (idempotent)
        mockMvc.perform(post("/bookings/" + bookingId + "/cancel/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("POST /bookings/{id}/cancel/ - cancelling CONFIRMED booking fails with 409 Conflict")
    public void testCancelConfirmedBookingFails() throws Exception {
        String token = registerUserAndGetToken("John", "john@example.com", "password123");

        Map<String, Object> body = Map.of(
                "centre_id", centre1.getId(),
                "test_id", test1.getId(),
                "appointment_datetime", futureDateTime()
        );

        String response = mockMvc.perform(post("/bookings/")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long bookingId = ((Number) objectMapper.readValue(response, Map.class).get("id")).longValue();

        // Mark booking CONFIRMED
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // Attempt cancellation
        mockMvc.perform(post("/bookings/" + bookingId + "/cancel/")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /bookings/ - pagination parameters size and page limit results")
    public void testGetUserBookingsPagination() throws Exception {
        String token = registerUserAndGetToken("John", "john@example.com", "password123");

        // Create 3 bookings
        for (int i = 1; i <= 3; i++) {
            Map<String, Object> body = Map.of(
                    "centre_id", centre1.getId(),
                    "test_id", test1.getId(),
                    "appointment_datetime", OffsetDateTime.now().plusDays(i).toString()
            );
            mockMvc.perform(post("/bookings/")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isCreated());
        }

        // Request page 0 with size 2
        mockMvc.perform(get("/bookings/?page=0&size=2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)));

        // Request page 1 with size 2 -> should return remaining 1
        mockMvc.perform(get("/bookings/?page=1&size=2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));
    }
}
