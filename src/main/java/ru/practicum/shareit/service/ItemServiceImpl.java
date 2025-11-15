package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemRepository;
import ru.practicum.shareit.item.model.CommentRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        userService.getUser(ownerId);

        Item item = ItemMapper.toItem(itemDto, ownerId);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!existingItem.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Access denied: only owner can update item");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemWithBookingsDto> getUserItems(Long ownerId) {
        userService.getUser(ownerId);

        List<Item> items = itemRepository.findByOwnerId(ownerId);
        LocalDateTime now = LocalDateTime.now();

        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        Map<Long, List<CommentDto>> commentsByItemId = getCommentsByItemIds(itemIds);

        return items.stream()
                .map(item -> {
                    ItemWithBookingsDto itemWithBookings = new ItemWithBookingsDto();
                    itemWithBookings.setId(item.getId());
                    itemWithBookings.setName(item.getName());
                    itemWithBookings.setDescription(item.getDescription());
                    itemWithBookings.setAvailable(item.getAvailable());
                    itemWithBookings.setComments(commentsByItemId.getOrDefault(item.getId(), new ArrayList<>()));

                    List<Booking> lastBookings = bookingRepository.findLastBooking(item.getId(), now);
                    if (!lastBookings.isEmpty()) {
                        BookingDto lastBookingDto = BookingMapper.toBookingDto(lastBookings.get(0));
                        itemWithBookings.setLastBooking(lastBookingDto);
                    }

                    List<Booking> nextBookings = bookingRepository.findNextBooking(item.getId(), now);
                    if (!nextBookings.isEmpty()) {
                        BookingDto nextBookingDto = BookingMapper.toBookingDto(nextBookings.get(0));
                        itemWithBookings.setNextBooking(nextBookingDto);
                    }

                    return itemWithBookings;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        String searchText = text.toLowerCase();
        return itemRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndAvailableIsTrue(
                        searchText, searchText)
                .stream()
                .filter(Item::getAvailable)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public CommentDto addComment(Long itemId, CommentCreateDto commentCreateDto, Long userId) {
        userService.getUser(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        List<Booking> userBookings = bookingRepository.findByBookerIdAndItemIdAndEndBefore(userId, itemId, LocalDateTime.now());

        boolean hasBooked = userBookings.stream()
                .anyMatch(booking -> booking.getStatus() == BookingStatus.APPROVED &&
                        booking.getEnd().isBefore(LocalDateTime.now()));

        if (!hasBooked) {
            throw new IllegalArgumentException("User can only comment on items they have booked in the past");
        }

        Comment comment = CommentMapper.toComment(commentCreateDto, itemId, userId);
        Comment savedComment = commentRepository.save(comment);

        UserDto author = userService.getUser(userId);
        return CommentMapper.toCommentDto(savedComment, author);
    }

    public ItemWithBookingsDto getItemWithBookingsAndComments(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        ItemWithBookingsDto itemWithBookings = ItemMapper.toItemWithBookingsDto(item);

        List<Comment> comments = commentRepository.findByItemId(itemId);
        List<CommentDto> commentDtos = comments.stream()
                .map(comment -> {
                    UserDto author = userService.getUser(comment.getAuthorId());
                    return CommentMapper.toCommentDto(comment, author);
                })
                .collect(Collectors.toList());

        itemWithBookings.setComments(commentDtos);

        if (item.getOwnerId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            List<Booking> lastBookings = bookingRepository.findLastBooking(itemId, now);
            if (!lastBookings.isEmpty()) {
                BookingDto lastBookingDto = BookingMapper.toBookingDto(lastBookings.get(0));
                itemWithBookings.setLastBooking(lastBookingDto);
            }

            List<Booking> nextBookings = bookingRepository.findNextBooking(itemId, now);
            if (!nextBookings.isEmpty()) {
                BookingDto nextBookingDto = BookingMapper.toBookingDto(nextBookings.get(0));
                itemWithBookings.setNextBooking(nextBookingDto);
            }
        }

        return itemWithBookings;
    }

    private Map<Long, List<CommentDto>> getCommentsByItemIds(List<Long> itemIds) {
        List<Comment> comments = commentRepository.findByItemIdIn(itemIds);

        return comments.stream()
                .collect(Collectors.groupingBy(
                        Comment::getItemId,
                        Collectors.mapping(comment -> {
                            UserDto author = userService.getUser(comment.getAuthorId());
                            return CommentMapper.toCommentDto(comment, author);
                        }, Collectors.toList())
                ));
    }
}