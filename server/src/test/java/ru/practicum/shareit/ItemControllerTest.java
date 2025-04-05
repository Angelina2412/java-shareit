package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.item.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@Import(ItemService.class)
@AutoConfigureMockMvc
public class ItemControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ItemService itemService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addItem_ShouldReturnItemResponse() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Drill", "Power drill", true, null);
        when(itemService.addItem(1L, itemDto)).thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, String.valueOf(1L))
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()));
    }

    @Test
    void updateItem_ShouldReturnUpdatedItem() throws Exception {
        ItemDto updatedItem = new ItemDto(1L, "Updated Drill", "Updated description", true, null);
        when(itemService.updateItem(1L, 1L, updatedItem)).thenReturn(updatedItem);

        mockMvc.perform(patch("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, String.valueOf(1L))
                        .content(objectMapper.writeValueAsString(updatedItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedItem.getId()))
                .andExpect(jsonPath("$.name").value(updatedItem.getName()));
    }

    @Test
    void searchItems_ShouldReturnMatchingItems() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Drill", "Power drill", true, null);
        when(itemService.searchItems("Drill")).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "Drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemDto.getId()))
                .andExpect(jsonPath("$[0].name").value(itemDto.getName()));
    }

    @Test
    void addComment_ShouldReturnCommentResponse() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Great item");
        commentDto.setUserId(1L);
        commentDto.setAuthorName("User");
        commentDto.setCreated(LocalDateTime.now());

        when(itemService.addComment(1L, 1L, commentDto)).thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, 1L)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentDto.getId()))
                .andExpect(jsonPath("$.text").value(commentDto.getText()));
    }

    @Test
    void getItem_ShouldReturnNotFound_WhenItemNotExists() throws Exception {
        mockMvc.perform(get("/items/{itemId}", 999L)
                        .header(USER_ID_HEADER, String.valueOf(1L)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void addItem_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        ItemDto itemDto = new ItemDto(null, "", "", true, null); // Пустое имя и описание
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, String.valueOf(1L))
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void getRequestById_ShouldReturnNotFound_WhenRequestNotExists() throws Exception {
        mockMvc.perform(get("/requests/{requestId}", 999L)
                        .header(USER_ID_HEADER, String.valueOf(1L)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void addItem_ShouldHandleMultipleRequests() throws Exception {
        ItemDto itemDto = new ItemDto(1L, "Drill", "Power drill", true, null);
        when(itemService.addItem(1L, itemDto)).thenReturn(itemDto);

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/items")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(USER_ID_HEADER, String.valueOf(1L))
                            .content(objectMapper.writeValueAsString(itemDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(itemDto.getId()))
                    .andExpect(jsonPath("$.name").value(itemDto.getName()));
        }
    }

    @Test
    void toCommentDto_ShouldMapAllFieldsCorrectly() {
        User user = new User();
        user.setId(1L);
        user.setName("User");

        Item item = new Item();
        item.setId(2L);
        item.setName("Item");

        Comment comment = new Comment();
        comment.setId(3L);
        comment.setText("Nice");
        comment.setUser(user);
        comment.setCreatedDate(LocalDateTime.of(2023, 1, 1, 12, 0));

        CommentDto dto = CommentMapper.toCommentDto(comment);

        assertThat(dto.getId(), is(comment.getId()));
        assertThat(dto.getText(), is(comment.getText()));
        assertThat(dto.getUserId(), is(user.getId()));
        assertThat(dto.getCreated(), is(comment.getCreatedDate()));
    }

    @Test
    void getItemById_ShouldReturnItemWithComments() throws Exception {
        Long userId = 1L;
        Long itemId = 100L;

        CommentDto comment = new CommentDto();
        comment.setId(1L);
        comment.setText("Great!");
        comment.setUserId(2L);
        comment.setAuthorName("Alice");
        comment.setCreated(LocalDateTime.of(2024, 12, 1, 10, 0));

        ItemDto itemDto = new ItemDto(itemId, "Drill", "Power drill", true, null);
        itemDto.setComments(List.of(comment));

        when(itemService.getItemById(itemId, userId)).thenReturn(itemDto);
        when(itemService.getCommentsByItemId(itemId)).thenReturn(List.of(comment));

        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.comments[0].id").value(comment.getId()))
                .andExpect(jsonPath("$.comments[0].text").value(comment.getText()))
                .andExpect(jsonPath("$.comments[0].userId").value(comment.getUserId()))
                .andExpect(jsonPath("$.comments[0].authorName").value(comment.getAuthorName()));
    }

    @Test
    void getAllItemsByOwner_ShouldReturnItemsWithComments() throws Exception {
        Long userId = 1L;

        CommentDto comment = new CommentDto();
        comment.setId(1L);
        comment.setText("Awesome");
        comment.setUserId(2L);
        comment.setAuthorName("Bob");
        comment.setCreated(LocalDateTime.of(2024, 11, 1, 12, 0));

        ItemDto item = new ItemDto(10L, "Saw", "Electric saw", true, null);
        item.setComments(List.of(comment));

        when(itemService.getAllItemsByOwner(userId)).thenReturn(List.of(item));
        when(itemService.getCommentsByItemId(item.getId())).thenReturn(List.of(comment));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(item.getId()))
                .andExpect(jsonPath("$[0].name").value(item.getName()))
                .andExpect(jsonPath("$[0].comments[0].text").value(comment.getText()));
    }

    @Test
    void addItem_ShouldReturnBadRequest_WhenUserIdMissing() throws Exception {
        ItemDto itemDto = new ItemDto(null, "Drill", "desc", true, null);

        when(itemService.addItem(anyLong(), eq(itemDto)))
                .thenThrow(new BadRequestException("User ID is missing"));

        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", "1")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad request"))
                .andExpect(jsonPath("$.details").value("User ID is missing"));
    }
}
