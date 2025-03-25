package com.shop.service;

import com.shop.entity.ChatMessage;
import com.shop.entity.ChatRoom;
import com.shop.entity.Member;
import com.shop.repository.ChatMessageRepository;
import com.shop.repository.ChatRoomRepository;
import com.shop.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository, MemberRepository memberRepository,
                           ChatMessageRepository chatMessageRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.memberRepository = memberRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    public ChatRoom getOrCreateChatRoom(Member user, Member admin) {
        // 사용자와 관리자가 동일한 채팅방을 찾음
        Optional<ChatRoom> existingChatRoom = chatRoomRepository.findByUserAndAdmin(user, admin);
        return existingChatRoom.orElseGet(() -> {
            // 채팅방 생성
            ChatRoom newChatRoom = new ChatRoom();
            newChatRoom.setUser(user);
            newChatRoom.setAdmin(admin);
            return chatRoomRepository.save(newChatRoom);
        });
    }

    public ChatRoom createChatRoom(Member user, Member admin) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setUser(user);
        chatRoom.setAdmin(admin);
        return chatRoomRepository.save(chatRoom);
    }

    public Optional<ChatRoom> findChatRoomById(Long id) {
        return chatRoomRepository.findById(id);
    }

    public List<ChatRoom> getActiveChatRooms(){
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        System.out.println("Active Chat Rooms : " + chatRooms);
        return chatRooms;
    }

    public List<ChatMessage> getMessagesByChatRoomId(Long chatRoomId){
        return chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(chatRoomId);
    }

    public ChatRoom getChatRoomById(Long id){
        return chatRoomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
    }
}
