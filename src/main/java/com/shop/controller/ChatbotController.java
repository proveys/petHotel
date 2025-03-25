package com.shop.controller;

import com.shop.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/chatbot")
public class ChatbotController {

   @Autowired
   private ChatbotService chatbotService;

   //POST 요청을 처리 : 사용자 입력 처리

   @PostMapping("/ask")
   @ResponseBody // JSON 형태로 응답
   public String ask(@RequestBody String userInput){
      return chatbotService.getResponse(userInput);
   }

   // GET 요청을 처리 : chatbot.html 렌더링
   @GetMapping
   public String chatbotPage(){
      return "chat/chatbot";
   }
}
