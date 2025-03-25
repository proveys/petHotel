package com.shop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.dto.OrderDto;
import com.shop.service.IamportTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortOneService {

   private static final String API_BASE_URL = "https://api.iamport.kr";
   private static final String API_KEY = "3373244601176317"; // 아임포트 API 키
   private static final String API_SECRET = "EWEMzSvdjnOrPgiPacKtixGViIX1aqjo4cPTHNWUu2LENiSXnLfSNYNj3E46iWv1azpQ3eh2sUaRI7Gw"; // 아임포트 API 시크릿 키

   private final RestTemplate restTemplate; // API 호출을 위한 RestTemplate
   private final ObjectMapper objectMapper; // JSON 변환을 위한 ObjectMapper
   private final IamportTokenService iamportTokenService; // 포트원 토큰 서비스

   // 환불 요청 처리 메서드
   public void cancelPayment(String payNo, String reason) {
      log.info("환불 요청 시작 - PayNo: {}, Reason: {}", payNo, reason);

      try {
         // 포트원 액세스 토큰 가져오기
         String accessToken = iamportTokenService.getToken();
         HttpHeaders headers = new HttpHeaders();
         headers.set("Authorization", "Bearer " + accessToken);
         headers.setContentType(MediaType.APPLICATION_JSON);

         // 요청 본문 데이터 생성
         Map<String, Object> body = new HashMap<>();
         body.put("merchant_uid", payNo); // 결제 고유 번호
         body.put("reason", reason);     // 환불 사유

         // HTTP 요청 객체 생성
         HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

         // 포트원 환불 API 호출
         ResponseEntity<String> response = restTemplate.exchange(
                 "https://api.iamport.kr/payments/cancel",
                 HttpMethod.POST,
                 requestEntity,
                 String.class
         );

         // 응답 처리
         if (response.getStatusCode() != HttpStatus.OK) {

            log.error("환불 요청 실패 - 응답: {}", response.getBody());
            throw new RuntimeException("환불 요청 실패: " + response.getBody());
         }

         log.info("환불 요청 성공 - PayNo: {}", payNo);
      } catch (Exception e) {
         log.error("환불 요청 중 오류 발생", e);
         throw new RuntimeException("환불 요청 중 오류 발생", e);
      }
   }

   // 결제 처리 메서드
   public void processPayment(OrderDto orderDto, String payNo) {
      log.info("결제 요청 시작 - PayNo: {}", payNo);

      try {
         // 포트원 액세스 토큰 가져오기
         String accessToken = iamportTokenService.getToken();

         HttpHeaders headers = new HttpHeaders();
         headers.set("Authorization", "Bearer " + accessToken);
         headers.setContentType(MediaType.APPLICATION_JSON);

         // 요청 본문 데이터 생성
         Map<String, Object> body = new HashMap<>();
         body.put("merchant_uid", payNo);               // 결제 고유 번호
         body.put("amount", orderDto.getTotalAmount()); // 결제 총 금액
         body.put("name", orderDto.getOrderName());     // 주문 이름

         // HTTP 요청 객체 생성
         HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

         // 포트원 결제 API 호출
         ResponseEntity<String> response = restTemplate.exchange(
                 "https://api.iamport.kr/payments/prepare",
                 HttpMethod.POST,
                 requestEntity,
                 String.class
         );

         // 응답 처리
         if (response.getStatusCode() != HttpStatus.OK) {
            log.error("결제 요청 실패 - 응답: {}", response.getBody());
            throw new RuntimeException("결제 요청 실패: " + response.getBody());
         }

         log.info("결제 요청 성공 - PayNo: {}", payNo);
      } catch (Exception e) {
         log.error("결제 요청 중 오류 발생", e);
         throw new RuntimeException("결제 요청 중 오류 발생", e);
      }
   }


   public String getAccessToken() {
      String url = API_BASE_URL + "/users/getToken";

      Map<String, String> body = new HashMap<>();
      body.put("imp_key", API_KEY);
      body.put("imp_secret", API_SECRET);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      ObjectMapper objectMapper = new ObjectMapper();

      System.out.println("API_KEY: " + API_KEY);
      System.out.println("API_SECRET: " + API_SECRET);
      try {
         String jsonBody = objectMapper.writeValueAsString(body);
         HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

         System.out.println("요청 URL: " + url);
         System.out.println("요청 본문: " + jsonBody);

         ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

         System.out.println("응답 상태 코드: " + response.getStatusCode());
         System.out.println("응답 본문: " + response.getBody());

         if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            Map<String, Object> responseData = (Map<String, Object>) responseBody.get("response");
            System.out.println("인증 토큰 획득 성공: " + responseData.get("access_token"));
            return (String) responseData.get("access_token");
         } else {
            System.err.println("인증 토큰 요청 실패: " + response.getBody());
            throw new IllegalStateException("포트원 인증 토큰 요청 실패");
         }
      } catch (Exception e) {
         e.printStackTrace();
         throw new IllegalStateException("getAccessToken() 중 오류 발생: " + e.getMessage(), e);
      }
   }

   // 2. 결제 정보 확인
   public Map<String, Object> getPaymentData(String impUid){
      String accessToken = getAccessToken();
      String url =API_BASE_URL + "/payments/" + impUid;

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(accessToken);

      HttpEntity<String> entity = new HttpEntity<>(headers);

      ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
      if (response.getStatusCode() == HttpStatus.OK) {
         Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("response");
         System.out.println("결제 데이터: " + responseData);
         return responseData;
      } else {
         System.err.println("결제 데이터 확인 실패: " + response.getBody());
         throw new IllegalStateException("결제 정보 확인 실패 : " + response.getBody());
      }
   }
}
