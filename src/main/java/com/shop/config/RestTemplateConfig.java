package com.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
//포트원 결제 시스템 관련 코드
   @Bean
   public RestTemplate restTemplate() {
      return new RestTemplate();
   }
}
