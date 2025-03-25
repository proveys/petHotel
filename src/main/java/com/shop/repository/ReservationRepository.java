package com.shop.repository;

import com.shop.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // 특정 사용자(userId)의 예약 목록 조회
    List<Reservation> findByUserId(Long userId);

    // 특정 날짜에 해당 객실(roomId)이 이미 예약되었는지 확인
    List<Reservation> findByRoomIdAndCheckinDateLessThanEqualAndCheckoutDateGreaterThanEqual(
            String roomId, LocalDate checkinDate, LocalDate checkoutDate);

    boolean existsByRoomIdAndCheckinDateAndCheckoutDate(String roomId, LocalDate checkinDate, LocalDate checkoutDate);
}
