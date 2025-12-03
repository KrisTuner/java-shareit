package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingRequestDtoJsonTest {

    @Autowired
    private JacksonTester<BookingRequestDto> json;

    @Autowired
    private ObjectMapper objectMapper;

    private BookingRequestDto bookingRequestDto;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        bookingRequestDto = new BookingRequestDto(
                1L,
                LocalDateTime.of(2024, 12, 25, 10, 0, 0),
                LocalDateTime.of(2024, 12, 26, 10, 0, 0)
        );
    }

    @Test
    void testSerialize() throws IOException {
        JsonContent<BookingRequestDto> result = json.write(bookingRequestDto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId")
                .isEqualTo(1);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        String expectedStart = bookingRequestDto.getStart().format(formatter);
        String expectedEnd = bookingRequestDto.getEnd().format(formatter);

        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo(expectedStart);
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo(expectedEnd);
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = "{\"itemId\":1,\"start\":\"2024-12-25T10:00:00\",\"end\":\"2024-12-26T10:00:00\"}";

        BookingRequestDto result = objectMapper.readValue(jsonContent, BookingRequestDto.class);

        assertThat(result.getItemId()).isEqualTo(1L);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2024, 12, 25, 10, 0, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2024, 12, 26, 10, 0, 0));
    }

    @Test
    void testDeserializeWithNullDates() throws IOException {
        String jsonContent = "{\"itemId\":1,\"start\":null,\"end\":null}";

        BookingRequestDto result = objectMapper.readValue(jsonContent, BookingRequestDto.class);

        assertThat(result.getItemId()).isEqualTo(1L);
        assertThat(result.getStart()).isNull();
        assertThat(result.getEnd()).isNull();
    }
}