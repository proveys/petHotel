package com.shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;

@Transactional
@Slf4j
@RequiredArgsConstructor
@Service
public class RefundService {


   public void refundRequest(String access_token, String merchant_uid, String reason) throws IOException {
      
      URL url = new URL("https://api.iamport.kr/payments/cancel");
      HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

      // 요청 방식을 POST로 설정
      conn.setRequestMethod("POST");

      // 요청의 Content-Type, Accept, Authorization 헤더 설정
      conn.setRequestProperty("Content-type", "application/json");
      conn.setRequestProperty("Accept", "application/json");
      conn.setRequestProperty("Authorization", access_token);

      // 나머지 로직 생략...
   }
}
