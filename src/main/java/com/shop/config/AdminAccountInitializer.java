package com.shop.config;

import com.shop.constant.Role;
import com.shop.entity.Member;
import com.shop.repository.MemberRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountInitializer(MemberRepository memberRepository, PasswordEncoder passwordEncoder){
        this.memberRepository=memberRepository;
        this.passwordEncoder=passwordEncoder;
    }

    @PostConstruct
    public void init() {
        // 관리자 계정이 이미 존재 하는지 확인
        if(memberRepository.findByEmail("admin@naver.com") == null){
            Member admin = new Member();
            admin.setName("관리자");
            admin.setEmail("admin@naver.com");
            admin.setPassword(passwordEncoder.encode("admin1234")); // 비밀번호 암호화
            admin.setRole(Role.ADMIN); // 관리자 역할 설정

            memberRepository.save(admin);
            System.out.println("관리자 계정 생성 완료 :   admin@naver.com ");
        }else{
            System.out.println("관리자 계정이 이미 존재합니다. ");
        }
    }
}