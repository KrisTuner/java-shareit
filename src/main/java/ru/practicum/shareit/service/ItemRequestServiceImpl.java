package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    public ItemRequestDto createItemRequest(ItemRequestCreateDto itemRequestCreateDto, Long userId) {
        userService.getUser(userId);

        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestCreateDto, userId);
        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);

        return ItemRequestMapper.toItemRequestDto(savedRequest, Collections.emptyList());
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        userService.getUser(userId);

        List<ItemRequest> requests = itemRequestRepository.findByRequesterId(userId);
        requests.sort((r1, r2) -> r2.getCreated().compareTo(r1.getCreated()));

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());
        Map<Long, List<ItemDto>> itemsByRequestId = getItemsByRequestIds(requestIds);

        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, int from, int size) {
        userService.getUser(userId);

        if (from < 0 || size <= 0) {
            throw new IllegalArgumentException("Invalid pagination parameters: from must be >= 0, size must be > 0");
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"));

        List<ItemRequest> requests = itemRequestRepository.findByRequesterIdNot(userId, pageable);

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());
        Map<Long, List<ItemDto>> itemsByRequestId = getItemsByRequestIds(requestIds);

        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        userService.getUser(userId);

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        List<ItemDto> items = getItemsByRequestId(requestId);

        return ItemRequestMapper.toItemRequestDto(request, items);
    }

    private Map<Long, List<ItemDto>> getItemsByRequestIds(List<Long> requestIds) {
        if (requestIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Item> items = itemRepository.findByRequestIdIn(requestIds);

        return items.stream()
                .collect(Collectors.groupingBy(
                        Item::getRequestId,
                        Collectors.mapping(ItemMapper::toItemDto, Collectors.toList())
                ));
    }

    private List<ItemDto> getItemsByRequestId(Long requestId) {
        List<Item> items = itemRepository.findByRequestId(requestId);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}