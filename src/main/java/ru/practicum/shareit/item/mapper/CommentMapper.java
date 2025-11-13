package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.user.dto.UserDto;

public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment, UserDto author) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                author.getName(),
                comment.getCreated()
        );
    }

    public static Comment toComment(CommentCreateDto commentCreateDto, Long itemId, Long authorId) {
        return new Comment(
                null,
                commentCreateDto.getText(),
                itemId,
                authorId,
                java.time.LocalDateTime.now()
        );
    }
}