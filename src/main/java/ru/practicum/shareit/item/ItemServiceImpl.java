package ru.practicum.shareit.item;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exceptions.BadRequestException;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;


    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository, BookingRepository bookingRepository, CommentRepository commentRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public ItemDto addItem(Long ownerId, ItemDto itemDto) {
        User user = userRepository.findById(ownerId)
                                  .orElseThrow(() -> new NotFoundException("Пользователь с ID " + ownerId + " не найден."));

        if (itemDto.getAvailable() == null) {
            throw new BadRequestException("Поле 'доступность' должно быть указано.");
        }

        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwnerId(ownerId);

        item = itemRepository.save(item);
        return toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long ownerId, Long itemId, ItemDto itemDto) throws AccessDeniedException {
        Item item = itemRepository.findById(itemId)
                                  .orElseThrow(() -> new NotFoundException("Item с ID " + itemId + " не найден."));

        if (!item.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Вы не являетесь владельцем этого Item.");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        item = itemRepository.save(item);
        return toItemDto(item);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                                  .orElseThrow(() -> new NotFoundException("Item с ID " + itemId + " не найден."));
        return toItemDtoWithBookings(item);
    }

    @Override
    public List<ItemDto> getAllItemsByOwner(Long ownerId) {
        List<Item> items = itemRepository.findByOwnerId(ownerId);
        return items.stream().map(this::toItemDto).collect(Collectors.toList());
    }

    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        List<Item> items = itemRepository.findByNameContainingIgnoreCaseAndAvailableTrue(text);

        return items.stream()
                    .map(this::toItemDto)
                    .collect(Collectors.toList());
    }




    @Override
    public List<ItemDto> getAllItemsWithBookingsByOwner(Long ownerId) {
        List<Item> items = itemRepository.findByOwnerId(ownerId);
        return items.stream()
                    .map(this::toItemDtoWithBookings)
                    .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) throws AccessDeniedException {
        Item item = itemRepository.findById(itemId)
                                  .orElseThrow(() -> new NotFoundException("Item с ID " + itemId + " не найден."));
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден."));

        boolean hasBooked = bookingRepository.existsByItemIdAndBookerIdAndEndBefore(itemId, userId, LocalDateTime.now());
        if (!hasBooked) {
            throw new BadRequestException("Вы не можете оставить отзыв, если не арендовали эту вещь.");
        }

        Comment comment = new Comment(item, user, commentDto.getText());
        comment = commentRepository.save(comment);

        return toCommentDto(comment);
    }


    @Override
    public List<CommentDto> getCommentsByItemId(Long itemId) {
        List<Comment> comments = commentRepository.findByItemId(itemId);
        return comments.stream().map(this::toCommentDto).collect(Collectors.toList());
    }

    private CommentDto toCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setUserId(comment.getUser().getId());
        dto.setAuthorName(comment.getUser().getName());
        dto.setCreated(comment.getCreatedDate());
        return dto;
    }

    private ItemDto toItemDtoWithBookings(Item item) {
        ItemDto dto = toItemDto(item);

        Booking lastBooking = bookingRepository.findLastBooking(item.getId());
        Booking nextBooking = bookingRepository.findNextBooking(item.getId());

        if (lastBooking != null) {
            dto.setLastBooking(lastBooking.getStart());
        }

        if (nextBooking != null) {
            dto.setNextBooking(nextBooking.getStart());
        }

        List<Comment> comments = commentRepository.findByItemId(item.getId());
        dto.setComments(comments.stream().map(this::toCommentDto).collect(Collectors.toList()));

        return dto;
    }


    private ItemDto toItemDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.isAvailable());
        return dto;
    }

    public Item getItemEntityById(Long itemId) {
        ItemDto itemDto = getItemById(itemId); // Получаем ItemDto
        return toEntity(itemDto); // Конвертируем в Item
    }

    private Item toEntity(ItemDto itemDto) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        return item;
    }

}
