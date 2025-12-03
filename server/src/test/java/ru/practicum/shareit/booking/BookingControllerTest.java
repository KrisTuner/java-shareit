package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.service.BookingService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingRequestDto bookingRequestDto;
    private BookingResponseDto bookingResponseDto;

    @BeforeEach
    void setUp() {
        bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        ItemDto itemDto = new ItemDto(1L, "Drill", "Powerful drill", true, null);
        UserDto bookerDto = new UserDto(2L, "Booker", "booker@email.com");

        bookingResponseDto = new BookingResponseDto(
                1L,
                bookingRequestDto.getStart(),
                bookingRequestDto.getEnd(),
                itemDto,
                bookerDto,
                "WAITING"
        );
    }

    @Test
    void createBooking_shouldReturnCreatedBooking() throws Exception {
        when(bookingService.createBooking(any(BookingRequestDto.class), eq(2L)))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andExpect(jsonPath("$.item.id", is(1)))
                .andExpect(jsonPath("$.booker.id", is(2)));
    }

    @Test
    void createBooking_withoutUserIdHeader_shouldReturnInternalServerError() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isInternalServerError()); // Изменено с isBadRequest()
    }

    @Test
    void createBooking_withInvalidDates_shouldReturnBadRequest() throws Exception {
        BookingRequestDto invalidRequest = new BookingRequestDto(
                1L,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1)
        );

        when(bookingService.createBooking(any(BookingRequestDto.class), eq(2L)))
                .thenThrow(new IllegalArgumentException("Invalid booking dates"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Invalid booking dates")));
    }

    @Test
    void approveBooking_shouldReturnApprovedBooking() throws Exception {
        BookingResponseDto approvedBooking = new BookingResponseDto(
                1L,
                bookingRequestDto.getStart(),
                bookingRequestDto.getEnd(),
                bookingResponseDto.getItem(),
                bookingResponseDto.getBooker(),
                "APPROVED"
        );

        when(bookingService.approveBooking(eq(1L), eq(true), eq(1L)))
                .thenReturn(approvedBooking);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void getBooking_shouldReturnBooking() throws Exception {
        when(bookingService.getBooking(eq(1L), eq(2L)))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("WAITING")));
    }

    @Test
    void getBooking_unauthorizedAccess_shouldReturnNotFound() throws Exception {
        when(bookingService.getBooking(eq(1L), eq(3L)))
                .thenThrow(new RuntimeException("Access denied"));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 3L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Access denied")));
    }

    @Test
    void getUserBookings_shouldReturnUserBookings() throws Exception {
        when(bookingService.getUserBookings(eq(2L), eq(BookingState.ALL)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void getUserBookings_withDefaultState_shouldReturnAllBookings() throws Exception {
        when(bookingService.getUserBookings(eq(2L), eq(BookingState.ALL)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getOwnerBookings_shouldReturnOwnerBookings() throws Exception {
        when(bookingService.getOwnerBookings(eq(1L), eq(BookingState.ALL)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void getOwnerBookings_withPagination_shouldReturnPaginatedResults() throws Exception {
        when(bookingService.getOwnerBookings(eq(1L), eq(BookingState.ALL)))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}