package com.shop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDto {

    @NotNull(message = "상품 아이디는 필수 입력 값입니다.")
    private Long itemId;

    private String itemNm;

    @Min(value= 1, message = "최소 주문 수량은 1개입니다.")
    @Max(value = 999, message = "최대 주문 수량은 999개 입니다.")
    private int count;

    private int price;

    @JsonProperty("imp_uid")
    @NotNull(message = "결제 고유 ID는 필수 입력 값입니다.")
    private String impUid;

    private int totalPrice;
    private int totalAmount;
    private String orderName;

}

