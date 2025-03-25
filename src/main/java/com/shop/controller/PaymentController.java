package com.shop.controller;

import com.shop.entity.Reservation;
import com.shop.service.PaymentService;
import com.shop.service.PortOneService;
import com.shop.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j

public class PaymentController {
   private final PaymentService paymentService;
   private final ReservationService reservationService;
   private final PortOneService portOneService;

   @Autowired
   public PaymentController(ReservationService reservationService, PortOneService portOneService, PaymentService paymentService){
      this. reservationService = reservationService;
      this. portOneService = portOneService;
      this. paymentService = paymentService;
   }



   @GetMapping("/payment")
   public String paymentPage(@RequestParam String roomId, @RequestBody int nights, @RequestParam int price, Model model){
      // 결제 정보 모델에 추가
      model.addAttribute("roomId", roomId);
      model.addAttribute("nights", nights);
      model.addAttribute("totalPrice", price*nights); // 총 금액 계산
      return "payment/payment";  // 결제 페이지로 이동
   }




   @PostMapping("/cancel")
   public ResponseEntity<String> cancelPayment(@RequestParam String payNo, @RequestParam(required = false) String reason) {
      HttpHeaders headers = new HttpHeaders();
      headers.add("Content-Type", "application/json;charset=UTF-8");
      try {
         paymentService.cancelPayment(payNo, reason != null ? reason : "사용자 요청"); // 기본 환불 사유 설정
         return ResponseEntity.ok("환불 요청이 성공적으로 처리되었습니다.");
      } catch (Exception e) {
         log.error("환불 요청 실패: {}", e.getMessage(), e);
         return ResponseEntity.internalServerError().body("환불 요청 처리 중 오류가 발생했습니다.");
      }
   }


   @PostMapping("/reservation/payment")
   public String processReservationPayment(@RequestBody Map<String, Object> paymentData, HttpSession session) {
      try {
         String impUid = (String) paymentData.get("imp_uid");
         int amount = (Integer) paymentData.get("amount");

         // 결제 검증 및 예약 생성
         paymentService.verifyPayment(impUid, amount);
         Reservation reservation = reservationService.confirmAndSaveReservation(paymentData, impUid, amount);

         System.out.println("Reservation Created: " + reservation); // 디버깅 로그

         session.setAttribute("reservation", reservation);
         return "redirect:/reservation/reservation-complete";
      } catch (Exception e) {
         e.printStackTrace();
         return "redirect:/error";
      }
   }
}


