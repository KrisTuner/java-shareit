package ru.practicum.shareit.booking;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    public static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final Map<Long, Booking> bookings = new HashMap<>();
    private long idCounter = 1;

    @PostMapping
    public BookingDto createBooking(@RequestBody BookingRequestDto bookingRequestDto,
                                    @RequestHeader(USER_ID_HEADER) Long userId) {
        Booking booking = BookingMapper.toBooking(bookingRequestDto, userId);
        booking.setId(idCounter++);
        booking.setStatus(BookingStatus.WAITING);
        bookings.put(booking.getId(), booking);

        return BookingMapper.toBookingDto(booking);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@PathVariable Long bookingId,
                                     @RequestParam boolean approved,
                                     @RequestHeader(USER_ID_HEADER) Long ownerId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new RuntimeException("Booking not found");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        return BookingMapper.toBookingDto(booking);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@PathVariable Long bookingId,
                                 @RequestHeader(USER_ID_HEADER) Long userId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new RuntimeException("Booking not found");
        }
        return BookingMapper.toBookingDto(booking);
    }

    @GetMapping
    public List<BookingDto> getUserBookings(@RequestHeader(USER_ID_HEADER) Long userId,
                                            @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookings.values().stream()
                .filter(booking -> booking.getBookerId().equals(userId))
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @GetMapping("/owner")
    public List<BookingDto> getOwnerBookings(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                             @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookings.values().stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }
}