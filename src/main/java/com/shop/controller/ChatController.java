package com.shop.controller;

import com.shop.entity.ChatMessage;
import com.shop.entity.ChatRoom;
import com.shop.entity.Member;
import com.shop.service.ChatRoomService;
import com.shop.service.MemberService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private final ChatRoomService chatRoomService;
    private final MemberService memberService;

    public ChatController(ChatRoomService chatRoomService, MemberService memberService) {
        this.chatRoomService = chatRoomService;
        this.memberService = memberService;
    }

    @GetMapping("/start")
    public String startChat(Authentication authentication, Model model) {
        String username = authentication.getName();
        System.out.println("Logged-in username :" + username);
        Member user = memberService.findByEmail(username);
        Member admin = memberService.findAdmin();

        ChatRoom chatRoom = chatRoomService.getOrCreateChatRoom(user, admin);
        model.addAttribute("chatRoom", chatRoom);

        return "chat/chatRoom"; // chatRoom.html 렌더링
    }

    @MessageMapping("/chat-room/{chatRoomId}/send")
    @SendTo("/topic/chat-room/{chatRoomId}")
    public ChatMessage sendMessage(@DestinationVariable Long chatRoomId, ChatMessage message,
                                   Authentication authentication) {

        String username = authentication.getName();
        Member sender = memberService.findByEmail(username);

        message.setSender(sender);
        message.setTimestamp(new Date());
        // 메시지 저장 로직 추가 가능
        return message; // 클라이언트로 메시지를 브로드캐스트
    }

    @GetMapping("/room/{id}")
    public String openChatRoom(@PathVariable Long id, Model model) {
        ChatRoom chatRoom = chatRoomService.getChatRoomById(id); // 채팅방 조회
        model.addAttribute("chatRoom", chatRoom); // 채팅방 정보 전달
        model.addAttribute("messages", chatRoomService.getMessagesByChatRoomId(id)); // 채팅 메시지 전달
        return "chat/chatRoom"; // chatRoom.html 렌더링
    }
}