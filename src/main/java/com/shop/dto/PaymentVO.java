package com.shop.dto;

import lombok.Data;

@Data
public class PaymentVO {
   private long canceledAt;
   private String failReason;
   private String receiptURL;
}