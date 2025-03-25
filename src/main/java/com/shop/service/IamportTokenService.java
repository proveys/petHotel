package com.shop.service;

import com.fasterxml.jackson.databind.ObjectMapper; //JSON 직렬화/역직렬화
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor

public class IamportTokenService {
   //포트원의 토큰 발급 API URL

   private static final String TOKEN_URL = "https://api.iamport.kr/users/getToken";
   //application.properties 에서 값을 읽어오는 애노테이션

   @Value("${imp.api.key}")
   private String apiKey; //포트원의 api key

   @Value("${imp.api.secretkey}")
   private String secretKey; // 포트원의 Secret Key

//토큰 발급 메소드
   public String getToken() {
      //로그로 요청 시작을 알림(key와 secret키가 입력되는지 확인가능)
      log.info("IAMPORT API 요청 시작 - Key: {}, Secret: {}", apiKey, secretKey);


      try {
         //1. 요청 본문 데이터 생성(Json 직렬화 준비)
         Map<String, String> keyMap = Map.of(
                 "imp_key", apiKey, //JSON의 "imp_key" 필드에 API key값을 매핑
                 "imp_secret", secretKey // JSON의 "imp_secret" 필드에 Secret Key값을 매핑
         );
         //2.RestTemplate 요청 준비(HTTP 요청을 보낼 객체 준비)
         RestTemplate restTemplate = new RestTemplate();
         HttpHeaders headers = new HttpHeaders(); //HTTP 요청의 헤더 정보
         headers.setContentType(MediaType.APPLICATION_JSON); //Content-Type을 JSON으로 설정

         //Map<String, String> body = Map.of("imp_key", apiKey, "imp_secret", secretKey);
         //3. 요청 데이터(JSON 직렬화된 형태)와 헤더를 결합하여 HttpEntity 객체 생성
         HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(keyMap, headers);
         // 4. POST 요청 전송 (RestTemplate를 사용해 API 호출)
         ResponseEntity<String> response = restTemplate.exchange(
                 TOKEN_URL,           // API URL
                 HttpMethod.POST,     // HTTP 메서드 (POST 요청)
                 requestEntity,       // 요청 본문 (JSON 직렬화된 데이터)
                 String.class         // 응답 데이터 형식 (JSON 문자열)
         );
         // 5. 응답 처리

            if (response.getStatusCode() == HttpStatus.OK) { // 응답 코드가 200 (성공)일 경우
               ObjectMapper objectMapper = new ObjectMapper(); // JSON 데이터를 Java 객체로 변환하기 위한 클래스
               // 응답 JSON 문자열을 Map으로 역직렬화
               Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
               // "response" 필드 내부 데이터 추출
               Map<String, String> result = (Map<String, String>) responseMap.get("response");

               // Access Token 로그 출력
               log.info("Access Token 발급 성공: {}", result.get("access_token"));
               return result.get("access_token"); // 발급된 Access Token 반환

         } else {
               // 응답 코드가 200이 아닌 경우 오류 로그 출력
               log.error("Access Token 발급 실패 - 응답 코드: {}", response.getStatusCode());
               throw new RuntimeException("Token 발급 실패");
         }

      } catch (Exception e) {  // 예외 발생 시 오류 로그 출력
         log.error("Access Token 요청 중 오류 발생: {}", e.getMessage(), e);
         throw new RuntimeException("Token 발급 실패", e);
      }

   }
}
