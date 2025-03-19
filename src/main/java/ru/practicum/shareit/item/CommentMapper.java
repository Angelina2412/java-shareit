package ru.practicum.shareit.item;

public class CommentMapper {
    public static CommentDto toCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setUserId(comment.getUser().getId());
        dto.setAuthorName(comment.getUser().getName());
        dto.setCreated(comment.getCreatedDate());
        return dto;
    }
}
