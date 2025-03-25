package com.shop.dto;

import lombok.Data;



@Data
public class CancelDto {
   private long canceledAt;
   private String failReason;
   private String receiptUrl;

   public static PaymentVO of(CancelDto cancelDto) {
      PaymentVO paymentVO = new PaymentVO();
      paymentVO.setCanceledAt(cancelDto.getCanceledAt());
      paymentVO.setFailReason(cancelDto.getFailReason());
      paymentVO.setReceiptURL(cancelDto.getReceiptUrl());

      return paymentVO;
   }
}

