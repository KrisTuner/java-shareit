package ru.practicum.shareit.request;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    public static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final Map<Long, ItemRequest> requests = new HashMap<>();
    private long idCounter = 1;

    private static final String DEFAULT_PAGE_START = "0";
    private static final String DEFAULT_PAGE_SIZE = "10";

    @PostMapping
    public ItemRequestDto createItemRequest(@RequestBody ItemRequestCreateDto itemRequestCreateDto,
                                            @RequestHeader(USER_ID_HEADER) Long userId) {
        ItemRequest request = ItemRequestMapper.toItemRequest(itemRequestCreateDto, userId);
        request.setId(idCounter++);
        requests.put(request.getId(), request);

        return ItemRequestMapper.toItemRequestDto(request);
    }

    @GetMapping
    public List<ItemRequestDto> getUserRequests(@RequestHeader(USER_ID_HEADER) Long userId) {
        return requests.values().stream()
                .filter(request -> request.getRequesterId().equals(userId))
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader(USER_ID_HEADER) Long userId,
                                               @RequestParam(defaultValue = DEFAULT_PAGE_START) int from,
                                               @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
        return requests.values().stream()
                .filter(request -> !request.getRequesterId().equals(userId))
                .map(ItemRequestMapper::toItemRequestDto)
                .toList();
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequest(@PathVariable Long requestId,
                                     @RequestHeader(USER_ID_HEADER) Long userId) {
        ItemRequest request = requests.get(requestId);
        if (request == null) {
            throw new RuntimeException("Request not found");
        }
        return ItemRequestMapper.toItemRequestDto(request);
    }
}