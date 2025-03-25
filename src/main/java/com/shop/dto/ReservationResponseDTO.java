package com.shop.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter

// 출력 용
@Data
public class ReservationResponseDTO {
    private Long id; // 예약 ID
    private Long userId; // 사용자 ID
    private String roomId; // 객실 ID
    private LocalDate checkinDate; // 입실일
    private LocalDate checkoutDate; // 퇴실일
    private int adultCount; // 성인 수
    private int childCount; // 아동 수
    private int totalPrice; // 총 결제 금액
    private LocalDateTime createdAt; // 예약 생성 시간
}