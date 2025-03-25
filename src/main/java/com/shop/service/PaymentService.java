package com.shop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
@RequiredArgsConstructor

@Slf4j
@Service
public class PaymentService {

   private final IamportTokenService iamportTokenService;
   private final PortOneService portOneService;

   public void cancelPayment(String merchantUid, String reason) {
      log.info("환불 요청 시작 - Merchant UID: {}, Reason: {}", merchantUid, reason);

      try {
         String accessToken = iamportTokenService.getToken();
         RestTemplate restTemplate = new RestTemplate();
         HttpHeaders headers = new HttpHeaders();
         headers.set("Authorization", "Bearer " + accessToken);
         headers.setContentType(MediaType.APPLICATION_JSON);

         Map<String, Object> body = new HashMap<>();
         body.put("merchant_uid", merchantUid);
         body.put("reason", reason);

         HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

         ResponseEntity<String> response = restTemplate.exchange(
                 "https://api.iamport.kr/payments/cancel",
                 HttpMethod.POST,
                 requestEntity,
                 String.class
         );

         if (response.getStatusCode() != HttpStatus.OK) {
            log.error("환불 요청 실패: {}", response.getBody());
            throw new RuntimeException("환불 요청 실패");
         }

         log.info("환불 요청 성공 - Merchant UID: {}", merchantUid);
      } catch (Exception e) {
         log.error("환불 요청 중 오류 발생", e);
         throw new RuntimeException("환불 요청 중 오류 발생", e);
      }
   }

   public void verifyPayment(String impUid, int amount) {
      try {
         String accessToken = portOneService.getAccessToken();
         String url = "https://api.iamport.kr/payments/" + impUid;

         HttpHeaders headers = new HttpHeaders();
         headers.setBearerAuth(accessToken);

         RestTemplate restTemplate = new RestTemplate();
         HttpEntity<String> entity = new HttpEntity<>(headers);

         ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
         if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            Map<String, Object> responseData = (Map<String, Object>) responseBody.get("response");

            int paidAmount = (int) responseData.get("amount");
            String status = (String) responseData.get("status");
            if (!"paid".equals(status)) {
               throw new IllegalStateException("결제가 완료되지 않았습니다.");
            }
            if (paidAmount != amount) {
               throw new IllegalStateException("결제 금액 불일치: 예상 " + amount + ", 실제 " + paidAmount);
            }
            System.out.println("결제 검증 완료");
         } else {
            throw new IllegalStateException("결제 검증 실패");
         }
      } catch (Exception e) {
         System.err.println("결제 검증 중 예외 발생 : " + e.getMessage());
         e.printStackTrace();
         throw new IllegalStateException("결제 검증 중 오류 발생 : "+ e.getMessage(),e);
      }
   }
}


















//   private static final String API_KEY = "imp.api.key";
//   private static final String API_SECRET = "imp.api.secretkey";
//
//   public void refundPayment(String paymentId, int amount) {
//      // 카카오페이 API 호출 예제
//      RestTemplate restTemplate = new RestTemplate();
//      HttpHeaders headers = new HttpHeaders();
//      headers.set("Authorization", "Bearer YOUR_ACCESS_TOKEN"); // 카카오페이 API 키
//      headers.setContentType(MediaType.APPLICATION_JSON);
//
//      Map<String, Object> body = new HashMap<>();
//      body.put("paymentId", paymentId);
//      body.put("cancel_amount", amount);
//
//      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
//      ResponseEntity<String> response = restTemplate.exchange(
//              "https://kapi.kakao.com/v1/payment/cancel",
//              HttpMethod.POST,
//              entity,
//              String.class
//      );
//
//      if (response.getStatusCode() != HttpStatus.OK) {
//         throw new IllegalStateException("환불 요청 실패: " + response.getBody());
//      }
//   }