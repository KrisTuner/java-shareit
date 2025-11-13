package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    public BookingResponseDto createBooking(BookingRequestDto bookingRequestDto, Long userId) {
        userService.getUser(userId);

        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Item is not available for booking");
        }

        if (item.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Owner cannot book their own item");
        }

        if (bookingRequestDto.getStart() == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }

        if (bookingRequestDto.getEnd() == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }

        if (bookingRequestDto.getStart().isAfter(bookingRequestDto.getEnd()) ||
                bookingRequestDto.getStart().isEqual(bookingRequestDto.getEnd())) {
            throw new IllegalArgumentException("Invalid booking dates");
        }

        if (bookingRequestDto.getStart().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }

        if (bookingRequestDto.getEnd().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("End date cannot be in the past");
        }

        Booking booking = BookingMapper.toBooking(bookingRequestDto, userId);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);

        ItemDto itemDto = itemService.getItem(item.getId());
        UserDto bookerDto = userService.getUser(userId);

        return BookingMapper.toBookingResponseDto(savedBooking, itemDto, bookerDto);
    }

    @Override
    public BookingResponseDto approveBooking(Long bookingId, boolean approved, Long ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Only item owner can approve booking");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException("Booking is not waiting for approval");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);

        ItemDto itemDto = itemService.getItem(item.getId());
        UserDto bookerDto = userService.getUser(updatedBooking.getBookerId());

        return BookingMapper.toBookingResponseDto(updatedBooking, itemDto, bookerDto);
    }

    @Override
    public BookingResponseDto getBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!booking.getBookerId().equals(userId) && !item.getOwnerId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        ItemDto itemDto = itemService.getItem(item.getId());
        UserDto bookerDto = userService.getUser(booking.getBookerId());

        return BookingMapper.toBookingResponseDto(booking, itemDto, bookerDto);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, BookingState state) {
        userService.getUser(userId);

        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        userId, now, now);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
                break;
            default:
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId);
        }

        return bookings.stream()
                .map(booking -> {
                    ItemDto itemDto = itemService.getItem(booking.getItemId());
                    UserDto bookerDto = userService.getUser(booking.getBookerId());
                    return BookingMapper.toBookingResponseDto(booking, itemDto, bookerDto);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, BookingState state) {
        userService.getUser(ownerId);

        List<Item> ownerItems = itemRepository.findByOwnerId(ownerId);
        if (ownerItems.isEmpty()) {
            throw new RuntimeException("User has no items");
        }

        List<Long> itemIds = ownerItems.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        List<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findByItemIdInAndStartBeforeAndEndAfterOrderByStartDesc(
                        itemIds, now, now);
                break;
            case PAST:
                bookings = bookingRepository.findByItemIdInAndEndBeforeOrderByStartDesc(itemIds, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemIdInAndStartAfterOrderByStartDesc(itemIds, now);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemIdInAndStatusOrderByStartDesc(itemIds, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemIdInAndStatusOrderByStartDesc(itemIds, BookingStatus.REJECTED);
                break;
            default:
                bookings = bookingRepository.findByItemIdInOrderByStartDesc(itemIds);
        }

        return bookings.stream()
                .map(booking -> {
                    ItemDto itemDto = itemService.getItem(booking.getItemId());
                    UserDto bookerDto = userService.getUser(booking.getBookerId());
                    return BookingMapper.toBookingResponseDto(booking, itemDto, bookerDto);
                })
                .collect(Collectors.toList());
    }
}