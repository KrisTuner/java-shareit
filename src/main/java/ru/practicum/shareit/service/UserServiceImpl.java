package ru.practicum.shareit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final Map<Long, User> users = new HashMap<>();
    private long idCounter = 1;

    @Override
    public UserDto createUser(UserDto userDto) {
        for (User user : users.values()) {
            if (user.getEmail().equals(userDto.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
        }

        User user = UserMapper.toUser(userDto);
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = users.get(userId);
        if (existingUser == null) {
            throw new RuntimeException("User not found");
        }

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            for (User user : users.values()) {
                if (user.getEmail().equals(userDto.getEmail()) && !user.getId().equals(userId)) {
                    throw new IllegalArgumentException("Email already exists");
                }
            }
        }

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }

        return UserMapper.toUserDto(existingUser);
    }

    @Override
    public UserDto getUser(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return users.values().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void deleteUser(Long userId) {
        users.remove(userId);
    }
}