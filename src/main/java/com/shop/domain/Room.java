package com.shop.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Room {
    private String id;  // 객실 ID
    private String name; // 객실 이름
    private int price;  // 가격
    private int minGuests; // 최소 인원
    private int maxGuests; // 최대 인원
}
