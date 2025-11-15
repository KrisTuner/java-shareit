package ru.practicum.shareit.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId);

    ItemDto getItem(Long itemId);

    List<ItemWithBookingsDto> getUserItems(Long ownerId);

    List<ItemDto> searchItems(String text);
}