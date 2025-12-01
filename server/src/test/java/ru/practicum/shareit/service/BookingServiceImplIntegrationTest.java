package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({BookingServiceImpl.class, ItemServiceImpl.class, UserServiceImpl.class})
class BookingServiceImplIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookingServiceImpl bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User booker;
    private Item availableItem;

    @BeforeEach
    void setUp() {
        owner = new User(null, "Owner", "owner@email.com");
        owner = userRepository.save(owner);

        booker = new User(null, "Booker", "booker@email.com");
        booker = userRepository.save(booker);

        availableItem = new Item(null, "Drill", "Powerful drill", true, owner.getId(), null);
        availableItem = itemRepository.save(availableItem);
    }

    @Test
    void createBooking_shouldCreateBookingSuccessfully() {
        Long itemId = availableItem.getId();
        BookingRequestDto requestDto = new BookingRequestDto(
                itemId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        BookingResponseDto booking = bookingService.createBooking(requestDto, booker.getId());

        assertNotNull(booking.getId());
        assertEquals(availableItem.getId(), booking.getItem().getId());
        assertEquals(booker.getId(), booking.getBooker().getId());
        assertEquals(BookingStatus.WAITING.name(), booking.getStatus());

        Booking savedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(BookingStatus.WAITING, savedBooking.getStatus());
    }

    @Test
    void createBooking_forUnavailableItem_shouldThrowException() {
        Item unavailableItem = new Item(null, "Broken", "Not working", false, owner.getId(), null);
        unavailableItem = itemRepository.save(unavailableItem);

        Long itemId = unavailableItem.getId();
        BookingRequestDto requestDto = new BookingRequestDto(
                itemId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(requestDto, booker.getId()));
        assertEquals("Item is not available for booking", exception.getMessage());
    }

    @Test
    void createBooking_byOwner_shouldThrowException() {
        Long itemId = availableItem.getId();
        BookingRequestDto requestDto = new BookingRequestDto(
                itemId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        Long ownerId = owner.getId();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(requestDto, ownerId));
        assertEquals("Owner cannot book their own item", exception.getMessage());
    }

    @Test
    void createBooking_withInvalidDates_shouldThrowException() {
        Long itemId = availableItem.getId();
        BookingRequestDto requestDto = new BookingRequestDto(
                itemId,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1)
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(requestDto, booker.getId()));
        assertEquals("Invalid booking dates", exception.getMessage());
    }

    @Test
    void approveBooking_shouldApproveSuccessfully() {
        Booking booking = new Booking(null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                availableItem.getId(),
                booker.getId(),
                BookingStatus.WAITING);
        booking = bookingRepository.save(booking);

        Long bookingId = booking.getId();
        Long ownerId = owner.getId();
        BookingResponseDto approvedBooking = bookingService.approveBooking(bookingId, true, ownerId);

        assertEquals(BookingStatus.APPROVED.name(), approvedBooking.getStatus());

        Booking updatedBooking = bookingRepository.findById(booking.getId()).orElseThrow();
        assertEquals(BookingStatus.APPROVED, updatedBooking.getStatus());
    }

    @Test
    void approveBooking_byNonOwner_shouldThrowException() {
        Booking booking = new Booking(null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                availableItem.getId(),
                booker.getId(),
                BookingStatus.WAITING);
        booking = bookingRepository.save(booking);

        User anotherUser = userRepository.save(new User(null, "Another", "another@email.com"));

        Long bookingId = booking.getId();
        Long anotherUserId = anotherUser.getId();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> bookingService.approveBooking(bookingId, true, anotherUserId));
        assertEquals("Only item owner can approve booking", exception.getMessage());
    }

    @Test
    void getBooking_shouldReturnBookingForBooker() {
        Booking booking = new Booking(null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                availableItem.getId(),
                booker.getId(),
                BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);

        Long bookingId = booking.getId();
        BookingResponseDto foundBooking = bookingService.getBooking(bookingId, booker.getId());

        assertEquals(booking.getId(), foundBooking.getId());
        assertEquals(availableItem.getId(), foundBooking.getItem().getId());
    }

    @Test
    void getBooking_shouldReturnBookingForOwner() {
        Booking booking = new Booking(null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                availableItem.getId(),
                booker.getId(),
                BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);

        Long bookingId = booking.getId();
        Long ownerId = owner.getId();
        BookingResponseDto foundBooking = bookingService.getBooking(bookingId, ownerId);

        assertEquals(booking.getId(), foundBooking.getId());
        assertEquals(availableItem.getId(), foundBooking.getItem().getId());
    }

    @Test
    void getBooking_byUnauthorizedUser_shouldThrowException() {
        Booking booking = new Booking(null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                availableItem.getId(),
                booker.getId(),
                BookingStatus.APPROVED);
        booking = bookingRepository.save(booking);

        User unauthorizedUser = userRepository.save(new User(null, "Stranger", "stranger@email.com"));

        Long bookingId = booking.getId();
        Long unauthorizedUserId = unauthorizedUser.getId();
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> bookingService.getBooking(bookingId, unauthorizedUserId));
        assertEquals("Access denied", exception.getMessage());
    }

    @Test
    void getUserBookings_shouldReturnUserBookings() {
        Booking pastBooking = new Booking(null,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(2),
                availableItem.getId(),
                booker.getId(),
                BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);

        Booking futureBooking = new Booking(null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                availableItem.getId(),
                booker.getId(),
                BookingStatus.WAITING);
        bookingRepository.save(futureBooking);

        Long bookerId = booker.getId();
        List<BookingResponseDto> userBookings = bookingService.getUserBookings(bookerId, BookingState.ALL);

        assertEquals(2, userBookings.size());
    }

    @Test
    void getUserBookings_withStatePast_shouldReturnOnlyPastBookings() {
        Booking pastBooking = new Booking(null,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(2),
                availableItem.getId(),
                booker.getId(),
                BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);

        Booking futureBooking = new Booking(null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                availableItem.getId(),
                booker.getId(),
                BookingStatus.WAITING);
        bookingRepository.save(futureBooking);

        Long bookerId = booker.getId();
        List<BookingResponseDto> pastBookings = bookingService.getUserBookings(bookerId, BookingState.PAST);

        assertEquals(1, pastBookings.size());
        assertEquals(pastBooking.getId(), pastBookings.get(0).getId());
    }

    @Test
    void getOwnerBookings_shouldReturnOwnerBookings() {
        Booking booking = new Booking(null,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                availableItem.getId(),
                booker.getId(),
                BookingStatus.APPROVED);
        bookingRepository.save(booking);

        Long ownerId = owner.getId();
        List<BookingResponseDto> ownerBookings = bookingService.getOwnerBookings(ownerId, BookingState.ALL);

        assertEquals(1, ownerBookings.size());
        assertEquals(booking.getId(), ownerBookings.get(0).getId());
    }

    @Test
    void getOwnerBookings_userWithoutItems_shouldThrowException() {
        User userWithoutItems = userRepository.save(new User(null, "NoItems", "noitems@email.com"));

        Long userId = userWithoutItems.getId();
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> bookingService.getOwnerBookings(userId, BookingState.ALL));
        assertEquals("User has no items", exception.getMessage());
    }
}