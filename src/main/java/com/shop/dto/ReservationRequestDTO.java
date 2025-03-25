package com.shop.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter

//  입력용
public class ReservationRequestDTO {
    private Long userId; // 사용자 ID
    private String roomId; // 객실 ID
    private LocalDate checkinDate; // 입실일
    private LocalDate checkoutDate; // 퇴실일
    private int adultCount; // 성인 수
    private int childCount; // 아동 수
    private int totalPrice; // 총 결제 금액
}