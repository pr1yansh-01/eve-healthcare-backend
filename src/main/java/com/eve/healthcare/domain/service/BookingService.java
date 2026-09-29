package com.eve.healthcare.domain.service;

import com.eve.healthcare.common.exception.BadRequestException;
import com.eve.healthcare.common.exception.ConflictException;
import com.eve.healthcare.common.exception.ResourceNotFoundException;
import com.eve.healthcare.domain.model.Booking;
import com.eve.healthcare.domain.model.BookingStatus;
import com.eve.healthcare.domain.model.CentreTest;
import com.eve.healthcare.domain.model.User;
import com.eve.healthcare.domain.repository.BookingRepository;
import com.eve.healthcare.domain.repository.CentreTestRepository;
import com.eve.healthcare.domain.repository.DiagnosticCentreRepository;
import com.eve.healthcare.domain.repository.UserRepository;
import com.eve.healthcare.web.dto.request.BookingCreateRequest;
import com.eve.healthcare.web.dto.response.BookingResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final DiagnosticCentreRepository centreRepository;
    private final CentreTestRepository centreTestRepository;

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            DiagnosticCentreRepository centreRepository,
            CentreTestRepository centreTestRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.centreRepository = centreRepository;
        this.centreTestRepository = centreTestRepository;
    }

    @Transactional
    public BookingResponse createBooking(Long userId, BookingCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!centreRepository.existsById(request.getCentreId())) {
            throw new ResourceNotFoundException("Centre not found with ID: " + request.getCentreId());
        }

        CentreTest centreTest = centreTestRepository
                .findByCentreIdAndTestId(request.getCentreId(), request.getTestId())
                .orElseThrow(() -> new ResourceNotFoundException("This test is not offered by the selected centre"));

        if (request.getAppointmentDatetime().isBefore(OffsetDateTime.now())) {
            throw new BadRequestException("Appointment datetime must be in the future");
        }

        // Snapshot the current price from the centre-test offering
        Booking booking = new Booking(
                user,
                centreTest,
                request.getAppointmentDatetime(),
                centreTest.getPrice()
        );

        Booking savedBooking = bookingRepository.save(booking);
        return BookingResponse.fromBooking(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(Long userId, int page, int size) {
        List<Booking> allBookings = bookingRepository.findByUserIdWithDetails(userId);
        int fromIndex = Math.min(Math.max(0, page) * size, allBookings.size());
        int toIndex = Math.min(fromIndex + Math.max(1, size), allBookings.size());
        return allBookings.subList(fromIndex, toIndex).stream()
                .map(BookingResponse::fromBooking)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (!booking.getUser().getId().equals(userId)) {
            // Consistent privacy policy: do not leak existence of other users' bookings
            throw new ResourceNotFoundException("Booking not found with ID: " + bookingId);
        }

        return BookingResponse.fromBooking(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (!booking.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Booking not found with ID: " + bookingId);
        }

        // Idempotent: already cancelled returns without error
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return BookingResponse.fromBooking(booking);
        }

        // Terminal state check: CONFIRMED bookings cannot be cancelled
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new ConflictException("Cannot cancel a CONFIRMED booking. Payment has already been successfully processed.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking updatedBooking = bookingRepository.save(booking);
        return BookingResponse.fromBooking(updatedBooking);
    }
}
