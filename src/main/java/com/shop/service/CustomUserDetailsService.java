package com.shop.service;

import com.shop.entity.Member;
import com.shop.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberService memberService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Email로 사용자 검색 : "+email);
        // 이메일로 Member 엔티티 검색
        Member member = memberRepository.findByEmail(email);
        if(member == null){
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다." + email);
        }
        return memberService.findByEmail(email);
    }
}