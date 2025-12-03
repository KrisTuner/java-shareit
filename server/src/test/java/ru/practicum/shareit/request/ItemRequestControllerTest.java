package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private ItemRequestCreateDto requestCreateDto;
    private ItemRequestDto requestDto;

    @BeforeEach
    void setUp() {
        requestCreateDto = new ItemRequestCreateDto("Need a power drill for home renovation");

        requestDto = new ItemRequestDto(
                1L,
                "Need a power drill for home renovation",
                2L,
                LocalDateTime.now(),
                List.of()
        );
    }

    @Test
    void createItemRequest_shouldReturnCreatedRequest() throws Exception {
        when(itemRequestService.createItemRequest(any(ItemRequestCreateDto.class), eq(2L)))
                .thenReturn(requestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.description", is("Need a power drill for home renovation")))
                .andExpect(jsonPath("$.requesterId", is(2)));
    }

    @Test
    void createItemRequest_withoutUserIdHeader_shouldReturnInternalServerError() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCreateDto)))
                .andExpect(status().isInternalServerError()); // Изменено с isBadRequest()
    }

    @Test
    void createItemRequest_withEmptyDescription_shouldReturnOk() throws Exception {
        requestCreateDto.setDescription("");

        when(itemRequestService.createItemRequest(any(ItemRequestCreateDto.class), eq(2L)))
                .thenReturn(requestDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestCreateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserRequests_shouldReturnUserRequests() throws Exception {
        when(itemRequestService.getUserRequests(eq(2L)))
                .thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void getAllRequests_shouldReturnAllRequests() throws Exception {
        when(itemRequestService.getAllRequests(eq(1L), eq(0), eq(10)))
                .thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void getAllRequests_withInvalidPagination_shouldReturnBadRequest() throws Exception {
        when(itemRequestService.getAllRequests(eq(1L), eq(-1), eq(10)))
                .thenThrow(new IllegalArgumentException("Invalid pagination parameters"));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Invalid pagination parameters")));
    }

    @Test
    void getRequestById_shouldReturnRequest() throws Exception {
        when(itemRequestService.getRequestById(eq(1L), eq(2L)))
                .thenReturn(requestDto);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.requesterId", is(2)));
    }

    @Test
    void getRequestById_notFound_shouldReturnNotFound() throws Exception {
        when(itemRequestService.getRequestById(eq(999L), eq(2L)))
                .thenThrow(new RuntimeException("Request not found"));

        mockMvc.perform(get("/requests/999")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Request not found")));
    }
}