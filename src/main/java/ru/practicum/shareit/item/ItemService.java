package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId);

    ItemDto getItem(Long itemId);

    List<ItemDto> getUserItems(Long ownerId);

    List<ItemDto> searchItems(String text);
}