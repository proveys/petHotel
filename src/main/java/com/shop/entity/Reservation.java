package com.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations") // 테이블 이름
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 예약 고유 ID (Primary Key)

    @Column(name = "user_id", nullable = false)
    private Long userId; // 사용자 ID

    @Column(name = "room_id", nullable = false, length = 10)
    private String roomId; // 객실 ID (101, 102 등)

    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate; // 입실일

    @Column(name = "checkout_date", nullable = false)
    private LocalDate checkoutDate; // 퇴실일

    @Column(name = "adult_count", nullable = false)
    private int adultCount; // 성인 수

    @Column(name = "child_count", nullable = false)
    private int childCount; // 아동 수

    @Column(name = "total_price", nullable = false)
    private int totalPrice; // 총 결제 금액

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 예약 생성 시간

    // 생성자 (편의를 위해 추가)
    public Reservation(Long userId, String roomId, LocalDate checkinDate, LocalDate checkoutDate,
                       int adultCount, int childCount, int totalPrice) {
        this.userId = userId;
        this.roomId = roomId;
        this.checkinDate = checkinDate;
        this.checkoutDate = checkoutDate;
        this.adultCount = adultCount;
        this.childCount = childCount;
        this.totalPrice = totalPrice;
        this.createdAt = LocalDateTime.now();
    }
}

