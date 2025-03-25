package com.shop.service;


import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;

@Service
public class ChatbotService {
   private final Random r = new Random();


   private final Map<String, String[]> responses = Map.of(
           "예약", new String[]{
                   "멍멍! 예약은 아래 링크를 통해 할 수 있어요! 🐶\n<a href='http://localhost:8081/reservation/view'>예약 페이지로 이동</a>",
                   "냐옹~ 아래 링크에서 예약하세요! 🐱\n<a href='http://localhost:8081/reservation/view'>예약하기</a>",
                   "삐약! 예약은 여기에서! 🐤\n<a href='http://localhost:8081/reservation/view'>예약하러 가기</a>"
           },
           "시간", new String[]{
                   "멍멍! 저희 운영시간은 9:00 ~ 18:00이에요! 🐶",
                   "냐옹~ 저희는 9시부터 18시까지 일하고 있어요! 🐱",
                   "삐약! 9시부터 18시까지 열려있답니다! 🐤"
           },
           "위치", new String[]{
                   "멍! 저희 위치는 인천 부평구에 있어요! 찾아오실 거죠? 🐾",
                   "냐옹~ 부평역 근처에서 저희를 찾을 수 있어요! 🐱",
                   "삐약삐약~ 부평에서 조금만 걸으면 만날 수 있어요! 🐤"
           },
           "연락처", new String[]{
                   "멍멍! 연락은 032-1234-5678로 부탁드려요! 🐶",
                   "냐옹~ 전화번호는 032-1234-5678이에요! 🐱",
                   "삐약! 032-1234-5678로 연락 주세요! 🐤"
           },
           "감사", new String[]{
                   "멍! 도와드릴 수 있어서 기뻐요! 🐾",
                   "냐옹~ 감사합니다! 다음에도 불러주세요! 🐱",
                   "삐약삐약~ 언제든지 도와드릴게요! 🐤"
           },
           "인사", new String[]{
                   "멍멍! 안녕하세요! 무엇을 도와드릴까요? 🐶",
                   "냐옹~ 안녕! 무슨 일이야? 🐱",
                   "삐약삐약~ 제가 도와줄게요! 🐤",
                   "멍멍! 원하는 게 뭐야? 말만 해! 🐾"
           },
           "서비스", new String[]{
                   "멍멍! 저희는 다음과 같은 서비스를 제공해요:\n- 반려동물 호텔\n- 반려동물 미용\n- 놀이 공간\n🐶",
                   "냐옹~ 저희의 주요 서비스는:\n- 반려동물 호텔\n- 놀이 공간 제공\n- 건강 체크\n🐱",
                   "삐약삐약~ 저희는 다양한 서비스를 제공하고 있어요! 🐤\n- 호텔\n- 놀이 공간\n- 건강 관리"
           },
           "이해못한답변", new String[]{
                   "멍? 무슨 뜻인지 모르겠어요... 다시 물어봐 주세요! 🐶",
                   "냐옹~ 이해 못했어요... 다른 질문 부탁드려요! 🐱",
                   "삐약? 잘 모르겠어요... 다시 말씀해 주세요! 🐤",
                   "멍? 뭐라고요? 저도 모르겠어요! 🐾"
           }

   );

   public String getResponse(String userInput) {
      for (Map.Entry<String, String[]> entry : responses.entrySet()) {
         String keyword = entry.getKey();
         String[] replies = entry.getValue();

         if (userInput.contains(keyword)) {
            return replies[r.nextInt(replies.length)];
         }
      }
      return responses.get("이해못한답변")[r.nextInt(responses.get("이해못한답변").length)];
   }

}
