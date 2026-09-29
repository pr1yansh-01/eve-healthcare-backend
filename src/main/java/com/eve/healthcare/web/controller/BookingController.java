package com.eve.healthcare.web.controller;

import com.eve.healthcare.common.security.SecurityUtils;
import com.eve.healthcare.domain.service.BookingService;
import com.eve.healthcare.web.dto.request.BookingCreateRequest;
import com.eve.healthcare.web.dto.response.BookingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@Tag(name = "Bookings", description = "Manage diagnostic test bookings")
@SecurityRequirement(name = "BearerAuth")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping({"", "/"})
    @Operation(summary = "Create a new diagnostic test booking")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        BookingResponse response = bookingService.createBooking(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping({"", "/"})
    @Operation(summary = "List all bookings for the authenticated user with optional pagination")
    public ResponseEntity<List<BookingResponse>> getUserBookings(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(bookingService.getUserBookings(userId, page, size));
    }

    @GetMapping({"/{booking_id}", "/{booking_id}/"})
    @Operation(summary = "Get booking details by ID")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable("booking_id") Long bookingId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(bookingService.getBookingById(bookingId, userId));
    }

    @PostMapping({"/{booking_id}/cancel", "/{booking_id}/cancel/"})
    @Operation(summary = "Cancel a diagnostic test booking")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable("booking_id") Long bookingId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId, userId));
    }
}
