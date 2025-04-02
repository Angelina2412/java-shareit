package ru.practicum.shareit.item;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerId(Long ownerId);

    List<Item> findByNameContainingIgnoreCaseAndAvailableTrue(String name);

    List<Item> findByItemRequest_Id(Long requestId);

    @Query("SELECT i FROM Item i WHERE i.owner.id <> :userId")
    Page<Item> findAllExcludingUser(@Param("userId") Long userId, Pageable pageable);
}
