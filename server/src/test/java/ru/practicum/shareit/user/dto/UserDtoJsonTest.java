package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSerialize() throws IOException {
        UserDto userDto = new UserDto(1L, "John Doe", "john.doe@email.com");

        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).extractingJsonPathNumberValue("$.id")
                .isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name")
                .isEqualTo("John Doe");
        assertThat(result).extractingJsonPathStringValue("$.email")
                .isEqualTo("john.doe@email.com");
    }

    @Test
    void testSerializeWithNullValues() throws IOException {
        UserDto userDto = new UserDto(null, null, null);

        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).extractingJsonPathNumberValue("$.id")
                .isNull();
        assertThat(result).extractingJsonPathStringValue("$.name")
                .isNull();
        assertThat(result).extractingJsonPathStringValue("$.email")
                .isNull();
    }

    @Test
    void testDeserialize() throws IOException {
        String jsonContent = "{\"id\":1,\"name\":\"John Doe\",\"email\":\"john.doe@email.com\"}";

        UserDto result = objectMapper.readValue(jsonContent, UserDto.class);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@email.com");
    }

    @Test
    void testDeserializePartial() throws IOException {
        String jsonContent = "{\"name\":\"John Doe\"}";

        UserDto result = objectMapper.readValue(jsonContent, UserDto.class);

        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isNull();
    }

    @Test
    void testDeserializeWithInvalidEmail() throws IOException {
        String jsonContent = "{\"id\":1,\"name\":\"John Doe\",\"email\":\"invalid-email\"}";

        UserDto result = objectMapper.readValue(jsonContent, UserDto.class);

        assertThat(result.getEmail()).isEqualTo("invalid-email");
    }
}