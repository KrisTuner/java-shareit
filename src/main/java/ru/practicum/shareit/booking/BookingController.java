package ru.practicum.shareit.booking;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static ru.practicum.shareit.booking.BookingConstants.*;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final Map<Long, Booking> bookings = new HashMap<>();
    private long idCounter = 1;

    @PostMapping
    public BookingDto createBooking(@RequestBody BookingRequestDto bookingRequestDto,
                                    @RequestHeader("X-Sharer-User-Id") Long userId) {
        Booking booking = BookingMapper.toBooking(bookingRequestDto, userId);
        booking.setId(idCounter++);
        booking.setStatus(BOOKING_STATUS_WAITING); // Используем константу
        bookings.put(booking.getId(), booking);

        return BookingMapper.toBookingDto(booking);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@PathVariable Long bookingId,
                                     @RequestParam boolean approved,
                                     @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new RuntimeException("Booking not found");
        }
        booking.setStatus(approved ? BOOKING_STATUS_APPROVED : BOOKING_STATUS_REJECTED);

        return BookingMapper.toBookingDto(booking);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@PathVariable Long bookingId,
                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new RuntimeException("Booking not found");
        }
        return BookingMapper.toBookingDto(booking);
    }

    @GetMapping
    public List<BookingDto> getUserBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                            @RequestParam(defaultValue = DEFAULT_BOOKING_STATE) String state) {
        return bookings.values().stream()
                .filter(booking -> booking.getBookerId().equals(userId))
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                             @RequestParam(defaultValue = DEFAULT_BOOKING_STATE) String state) {
        return bookings.values().stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }
}