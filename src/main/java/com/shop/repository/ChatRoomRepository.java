package com.shop.repository;

import com.shop.entity.ChatRoom;
import com.shop.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByUserAndAdmin(Member user, Member admin);
}
