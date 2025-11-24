package ru.practicum.shareit.request;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    public static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto createItemRequest(@RequestBody ItemRequestCreateDto itemRequestCreateDto,
                                            @RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.createItemRequest(itemRequestCreateDto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getUserRequests(@RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.getUserRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader(USER_ID_HEADER) Long userId,
                                               @RequestParam(defaultValue = "0") int from,
                                               @RequestParam(defaultValue = "10") int size) {
        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }
        return itemRequestService.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequest(@PathVariable Long requestId,
                                     @RequestHeader(USER_ID_HEADER) Long userId) {
        return itemRequestService.getRequestById(requestId, userId);
    }
}