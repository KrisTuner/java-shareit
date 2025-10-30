package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import static ru.practicum.shareit.booking.BookingConstants.BOOKING_STATUS_WAITING;

public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItemId(),
                booking.getBookerId(),
                booking.getStatus()
        );
    }

    public static Booking toBooking(BookingRequestDto bookingRequestDto, Long bookerId) {
        return new Booking(
                null,
                bookingRequestDto.getStart(),
                bookingRequestDto.getEnd(),
                bookingRequestDto.getItemId(),
                bookerId,
                BOOKING_STATUS_WAITING
        );
    }
}