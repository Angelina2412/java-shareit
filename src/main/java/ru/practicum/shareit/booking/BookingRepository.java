package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerId(Long bookerId);

    List<Booking> findAllByItemOwnerId(Long ownerId);

    List<Booking> findAllByItemId(Long itemId);
    boolean existsByItemIdAndBookerIdAndEndBefore(Long itemId, Long bookerId, LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.end < CURRENT_TIMESTAMP ORDER BY b.end DESC LIMIT 1")
    Booking findLastBooking(@Param("itemId") Long itemId);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.start > CURRENT_TIMESTAMP ORDER BY b.start ASC LIMIT 1")
    Booking findNextBooking(@Param("itemId") Long itemId);

}

