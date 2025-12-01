package ru.practicum.shareit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemRepository;
import ru.practicum.shareit.item.model.CommentRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.booking.Booking;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({ItemServiceImpl.class, UserServiceImpl.class, ItemRequestServiceImpl.class})
class ItemServiceImplIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = new User(null, "Owner", "owner@email.com");
        owner = userRepository.save(owner);

        booker = new User(null, "Booker", "booker@email.com");
        booker = userRepository.save(booker);

        item = new Item(null, "Item", "Description", true, owner.getId(), null);
        item = itemRepository.save(item);
    }

    @Test
    void createItem_shouldCreateItemSuccessfully() {
        ItemDto itemDto = new ItemDto(null, "New Item", "New Description", true, null);

        ItemDto createdItem = itemService.createItem(itemDto, owner.getId());

        assertNotNull(createdItem.getId());
        assertEquals("New Item", createdItem.getName());
        assertEquals("New Description", createdItem.getDescription());
        assertTrue(createdItem.getAvailable());

        Item savedItem = itemRepository.findById(createdItem.getId()).orElseThrow();
        assertEquals(owner.getId(), savedItem.getOwnerId());
    }

    @Test
    void createItem_withRequestId_shouldCreateItemWithRequest() {
        ItemRequest request = new ItemRequest(null, "Need a drill", booker.getId(), LocalDateTime.now());
        request = itemRequestRepository.save(request);

        ItemDto itemDto = new ItemDto(null, "Drill", "Powerful drill", true, request.getId());

        ItemDto createdItem = itemService.createItem(itemDto, owner.getId());

        assertNotNull(createdItem.getId());
        assertEquals(request.getId(), createdItem.getRequestId());
    }

    @Test
    void updateItem_shouldUpdateItemSuccessfully() {
        ItemDto updateDto = new ItemDto(null, "Updated Name", "Updated Description", false, null);

        ItemDto updatedItem = itemService.updateItem(item.getId(), updateDto, owner.getId());

        assertEquals("Updated Name", updatedItem.getName());
        assertEquals("Updated Description", updatedItem.getDescription());
        assertFalse(updatedItem.getAvailable());
    }

    @Test
    void updateItem_byNonOwner_shouldThrowException() {
        User anotherUser = userRepository.save(new User(null, "Another", "another@email.com"));
        ItemDto updateDto = new ItemDto(null, "Updated Name", "Updated Description", false, null);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> itemService.updateItem(item.getId(), updateDto, anotherUser.getId()));
        assertEquals("Access denied: only owner can update item", exception.getMessage());
    }

    @Test
    void getItem_shouldReturnItemSuccessfully() {
        ItemDto foundItem = itemService.getItem(item.getId());

        assertEquals(item.getId(), foundItem.getId());
        assertEquals(item.getName(), foundItem.getName());
        assertEquals(item.getDescription(), foundItem.getDescription());
    }

    @Test
    void getUserItems_shouldReturnItemsForOwner() {
        Item item2 = new Item(null, "Item 2", "Description 2", true, owner.getId(), null);
        itemRepository.save(item2);

        List<ItemWithBookingsDto> userItems = itemService.getUserItems(owner.getId());

        assertEquals(2, userItems.size());
        assertThat(userItems).extracting(ItemWithBookingsDto::getId)
                .containsExactlyInAnyOrder(item.getId(), item2.getId());
    }

    @Test
    void getUserItems_withBookingsAndComments_shouldIncludeThem() {
        Booking booking = new Booking(null,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                item.getId(),
                booker.getId(),
                BookingStatus.APPROVED);
        bookingRepository.save(booking);

        List<ItemWithBookingsDto> userItems = itemService.getUserItems(owner.getId());

        assertEquals(1, userItems.size());
        assertNotNull(userItems.get(0).getLastBooking());
        assertNull(userItems.get(0).getNextBooking());
    }

    @Test
    void searchItems_withBlankText_shouldReturnEmptyList() {
        List<ItemDto> results = itemService.searchItems("");

        assertTrue(results.isEmpty());
    }

    @Test
    void searchItems_withMatchingText_shouldReturnItems() {
        Item searchableItem = new Item(null, "Hammer", "Big hammer", true, owner.getId(), null);
        itemRepository.save(searchableItem);

        List<ItemDto> results = itemService.searchItems("hammer");

        assertEquals(1, results.size());
        assertEquals(searchableItem.getId(), results.get(0).getId());
    }

    @Test
    void searchItems_withUnavailableItem_shouldNotReturnIt() {
        Item unavailableItem = new Item(null, "Broken Drill", "Doesn't work", false, owner.getId(), null);
        itemRepository.save(unavailableItem);

        List<ItemDto> results = itemService.searchItems("drill");

        assertTrue(results.isEmpty());
    }

    @Test
    void addComment_shouldAddCommentSuccessfully() {
        Booking booking = new Booking(null,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(1),
                item.getId(),
                booker.getId(),
                BookingStatus.APPROVED);
        bookingRepository.save(booking);

        CommentCreateDto commentDto = new CommentCreateDto("Great item!");

        var comment = itemService.addComment(item.getId(), commentDto, booker.getId());

        assertNotNull(comment.getId());
        assertEquals("Great item!", comment.getText());
        assertEquals("Booker", comment.getAuthorName());

        boolean commentExists = commentRepository.existsByItemIdAndAuthorId(item.getId(), booker.getId());
        assertTrue(commentExists);
    }

    @Test
    void addComment_withoutPastBooking_shouldThrowException() {
        CommentCreateDto commentDto = new CommentCreateDto("Great item!");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> itemService.addComment(item.getId(), commentDto, booker.getId()));
        assertEquals("User can only comment on items they have booked in the past", exception.getMessage());
    }

    @Test
    void getItemWithBookingsAndComments_shouldReturnFullInfo() {
        Booking booking = new Booking(null,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                item.getId(),
                booker.getId(),
                BookingStatus.APPROVED);
        bookingRepository.save(booking);

        CommentCreateDto commentDto = new CommentCreateDto("Great!");
        itemService.addComment(item.getId(), commentDto, booker.getId());

        ItemWithBookingsDto itemWithDetails = itemService.getItemWithBookingsAndComments(item.getId(), owner.getId());

        assertEquals(item.getId(), itemWithDetails.getId());
        assertNotNull(itemWithDetails.getLastBooking());
        assertEquals(1, itemWithDetails.getComments().size());
        assertEquals("Great!", itemWithDetails.getComments().get(0).getText());
    }

    @Test
    void getItemsByRequest_shouldReturnItemsForRequest() {
        ItemRequest request = new ItemRequest(null, "Need tools", booker.getId(), LocalDateTime.now());
        request = itemRequestRepository.save(request);

        Item requestedItem = new Item(null, "Requested Item", "For request", true, owner.getId(), request.getId());
        itemRepository.save(requestedItem);

        List<ItemDto> items = itemService.getItemsByRequest(request.getId());

        assertEquals(1, items.size());
        assertEquals(requestedItem.getId(), items.get(0).getId());
        assertEquals(request.getId(), items.get(0).getRequestId());
    }
}