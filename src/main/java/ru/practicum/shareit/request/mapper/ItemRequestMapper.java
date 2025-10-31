package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;

public class ItemRequestMapper {

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getRequesterId(),
                itemRequest.getCreated()
        );
    }

    public static ItemRequest toItemRequest(ItemRequestCreateDto itemRequestCreateDto, Long requesterId) {
        return new ItemRequest(
                null,
                itemRequestCreateDto.getDescription(),
                requesterId,
                LocalDateTime.now()
        );
    }
}