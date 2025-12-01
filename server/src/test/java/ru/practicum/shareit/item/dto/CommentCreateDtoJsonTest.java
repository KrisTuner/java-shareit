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
class CommentCreateDtoJsonTest {

    @Autowired
    private JacksonTester<CommentCreateDto> json;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerialize() throws IOException {
        CommentCreateDto commentDto = new CommentCreateDto("Great item! Very useful.");

        JsonContent<CommentCreateDto> result = json.write(commentDto);

        assertThat(result).extractingJsonPathStringValue("$.text")
                .isEqualTo("Great item! Very useful.");
    }

    @Test
    void testSerializeEmptyText() throws IOException {
        CommentCreateDto commentDto = new CommentCreateDto("");

        JsonContent<CommentCreateDto> result = json.write(commentDto);

        assertThat(result).extractingJsonPathStringValue("$.text")
                .isEqualTo("");
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = "{\"text\":\"Excellent quality, would recommend!\"}";

        CommentCreateDto result = objectMapper.readValue(jsonContent, CommentCreateDto.class);

        assertThat(result.getText()).isEqualTo("Excellent quality, would recommend!");
    }

    @Test
    void testDeserializeWithNullText() throws IOException {
        String jsonContent = "{\"text\":null}";

        CommentCreateDto result = objectMapper.readValue(jsonContent, CommentCreateDto.class);

        assertThat(result.getText()).isNull();
    }

    @Test
    void testDeserializeWithMissingText() throws IOException {
        String jsonContent = "{}";

        CommentCreateDto result = objectMapper.readValue(jsonContent, CommentCreateDto.class);

        assertThat(result.getText()).isNull();
    }
}