package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.service.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(1L, "John Doe", "john@email.com");
    }

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        when(userService.createUser(any(UserDto.class)))
                .thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john@email.com")));
    }

    @Test
    void createUser_withDuplicateEmail_shouldReturnConflict() throws Exception {
        when(userService.createUser(any(UserDto.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already exists"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", containsString("Email already exists")));
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        UserDto updatedUser = new UserDto(1L, "John Updated", "john.updated@email.com");
        when(userService.updateUser(eq(1L), any(UserDto.class)))
                .thenReturn(updatedUser);

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Updated")))
                .andExpect(jsonPath("$.email", is("john.updated@email.com")));
    }

    @Test
    void getUser_shouldReturnUser() throws Exception {
        when(userService.getUser(eq(1L)))
                .thenReturn(userDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")));
    }

    @Test
    void getUser_notFound_shouldReturnNotFound() throws Exception {
        when(userService.getUser(eq(999L)))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("User not found")));
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        UserDto user2 = new UserDto(2L, "Jane Doe", "jane@email.com");
        when(userService.getAllUsers())
                .thenReturn(List.of(userDto, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    void deleteUser_shouldReturnOk() throws Exception {
        doNothing().when(userService).deleteUser(eq(1L));

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_notFound_shouldReturnNotFound() throws Exception {
        doThrow(new RuntimeException("User not found"))
                .when(userService).deleteUser(eq(999L));

        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("User not found")));
    }
}