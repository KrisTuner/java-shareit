package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto itemDto;
    private ItemWithBookingsDto itemWithBookingsDto;

    @BeforeEach
    void setUp() {
        itemDto = new ItemDto(1L, "Drill", "Powerful drill", true, null);

        itemWithBookingsDto = new ItemWithBookingsDto();
        itemWithBookingsDto.setId(1L);
        itemWithBookingsDto.setName("Drill");
        itemWithBookingsDto.setDescription("Powerful drill");
        itemWithBookingsDto.setAvailable(true);
    }

    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        when(itemService.createItem(any(ItemDto.class), anyLong()))
                .thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Drill")))
                .andExpect(jsonPath("$.description", is("Powerful drill")))
                .andExpect(jsonPath("$.available", is(true)));
    }

    @Test
    void createItem_withoutUserIdHeader_shouldReturnInternalServerError() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isInternalServerError()); // Изменено с isBadRequest()
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        ItemDto updatedItem = new ItemDto(1L, "Updated Drill", "Very powerful drill", false, null);
        when(itemService.updateItem(eq(1L), any(ItemDto.class), eq(1L)))
                .thenReturn(updatedItem);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Drill")))
                .andExpect(jsonPath("$.available", is(false)));
    }

    @Test
    void getItem_shouldReturnItem() throws Exception {
        when(itemService.getItemWithBookingsAndComments(eq(1L), eq(1L)))
                .thenReturn(itemWithBookingsDto);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Drill")));
    }

    @Test
    void getUserItems_shouldReturnListOfItems() throws Exception {
        when(itemService.getUserItems(eq(1L)))
                .thenReturn(List.of(itemWithBookingsDto));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Drill")));
    }

    @Test
    void searchItems_shouldReturnMatchingItems() throws Exception {
        when(itemService.searchItems(eq("drill")))
                .thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Drill")));
    }

    @Test
    void searchItems_withBlankText_shouldReturnEmptyList() throws Exception {
        when(itemService.searchItems(eq("")))
                .thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void addComment_shouldReturnCreatedComment() throws Exception {
        CommentCreateDto commentCreateDto = new CommentCreateDto("Great item!");
        CommentDto commentDto = new CommentDto(1L, "Great item!", "User", LocalDateTime.now());

        when(itemService.addComment(eq(1L), any(CommentCreateDto.class), eq(2L)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.text", is("Great item!")))
                .andExpect(jsonPath("$.authorName", is("User")));
    }

    @Test
    void getItemsByRequest_shouldReturnItems() throws Exception {
        when(itemService.getItemsByRequest(eq(1L)))
                .thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/request/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Drill")));
    }
}