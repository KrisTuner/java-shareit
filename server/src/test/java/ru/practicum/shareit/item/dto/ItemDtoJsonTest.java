package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerialize() throws IOException {
        ItemDto itemDto = new ItemDto(1L, "Drill", "Powerful drill", true, null);

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id")
                .isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name")
                .isEqualTo("Drill");
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Powerful drill");
        assertThat(result).extractingJsonPathBooleanValue("$.available")
                .isEqualTo(true);
        assertThat(result).extractingJsonPathNumberValue("$.requestId")
                .isNull();
    }

    @Test
    void testSerializeWithRequestId() throws IOException {
        ItemDto itemDto = new ItemDto(1L, "Drill", "Powerful drill", true, 100L);

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.requestId")
                .isEqualTo(100);
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = "{\"id\":1,\"name\":\"Drill\",\"description\":\"Powerful drill\",\"available\":true,\"requestId\":100}";

        ItemDto result = objectMapper.readValue(jsonContent, ItemDto.class);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Drill");
        assertThat(result.getDescription()).isEqualTo("Powerful drill");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getRequestId()).isEqualTo(100L);
    }

    @Test
    void testDeserializeWithNullAvailable() throws IOException {
        String jsonContent = "{\"id\":1,\"name\":\"Drill\",\"description\":\"Powerful drill\",\"available\":null}";

        ItemDto result = objectMapper.readValue(jsonContent, ItemDto.class);

        assertThat(result.getAvailable()).isNull();
    }

    @Test
    void testDeserializeWithBooleanString() throws IOException {
        String jsonContent = "{\"id\":1,\"name\":\"Drill\",\"description\":\"Powerful drill\",\"available\":\"true\"}";

        ItemDto result = objectMapper.readValue(jsonContent, ItemDto.class);

        assertThat(result.getAvailable()).isTrue();
    }
}