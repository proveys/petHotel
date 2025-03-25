package com.shop.service;

import com.shop.entity.ChatMessage;
import com.shop.entity.ChatRoom;
import com.shop.entity.Member;
import com.shop.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public ChatMessage saveMessage(ChatRoom chatRoom, Member sender, String content) {
        ChatMessage message = new ChatMessage();
        message.setChatRoom(chatRoom);
        message.setSender(sender);
        message.setContent(content);
        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> findMessagesByChatRoom(ChatRoom chatRoom) {
        return chatMessageRepository.findAll() // 추후 필요시 쿼리 메서드 추가
                .stream()
                .filter(message -> message.getChatRoom().equals(chatRoom))
                .collect(Collectors.toList());
    }
}
