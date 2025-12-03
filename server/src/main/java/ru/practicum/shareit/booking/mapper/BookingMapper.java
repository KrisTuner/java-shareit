package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

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

    public static BookingResponseDto toBookingResponseDto(Booking booking, ItemDto item, UserDto booker) {
        return new BookingResponseDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                item,
                booker,
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