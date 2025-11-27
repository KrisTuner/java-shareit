package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime end);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime start);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    List<Booking> findByItemIdInOrderByStartDesc(List<Long> itemIds);

    List<Booking> findByItemIdInAndStartBeforeAndEndAfterOrderByStartDesc(
            List<Long> itemIds, LocalDateTime start, LocalDateTime end);

    List<Booking> findByItemIdInAndEndBeforeOrderByStartDesc(List<Long> itemIds, LocalDateTime end);

    List<Booking> findByItemIdInAndStartAfterOrderByStartDesc(List<Long> itemIds, LocalDateTime start);

    List<Booking> findByItemIdInAndStatusOrderByStartDesc(List<Long> itemIds, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.itemId = :itemId AND b.status = 'APPROVED' " +
            "AND b.end > :currentTime ORDER BY b.start ASC")
    List<Booking> findNextBooking(@Param("itemId") Long itemId, @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b WHERE b.itemId = :itemId AND b.status = 'APPROVED' " +
            "AND b.end <= :currentTime ORDER BY b.end DESC")
    List<Booking> findLastBooking(@Param("itemId") Long itemId, @Param("currentTime") LocalDateTime currentTime);

    List<Booking> findByBookerIdAndItemIdAndEndBefore(Long bookerId, Long itemId, LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE b.bookerId = :bookerId AND b.itemId = :itemId " +
            "AND b.end < :currentTime AND b.status = 'APPROVED'")
    List<Booking> findApprovedPastBookingsByBookerAndItem(@Param("bookerId") Long bookerId,
                                                          @Param("itemId") Long itemId,
                                                          @Param("currentTime") LocalDateTime currentTime);
}