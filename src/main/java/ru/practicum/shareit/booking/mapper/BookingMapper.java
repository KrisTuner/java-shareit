package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItemId(),
                booking.getBookerId(),
                booking.getStatus().name()
        );
    }

    public static Booking toBooking(BookingRequestDto bookingRequestDto, Long bookerId) {
        return new Booking(
                null,
                bookingRequestDto.getStart(),
                bookingRequestDto.getEnd(),
                bookingRequestDto.getItemId(),
                bookerId,
                BookingStatus.WAITING
        );
    }
}