package com.shop.service;

import com.shop.constant.Role;
import com.shop.entity.Member;
import com.shop.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    // 회원 저장
    // 회원 저장 메서드
    public Member saveMember(Member member) {
        // 중복 회원 검사
        validateDuplicateMember(member);
        // 기본 역할 설정
        member.setRole(Role.USER);
        // 회원 저장
        return memberRepository.save(member);
    }

    // 중복 회원 확인
    private void validateDuplicateMember(Member member) {
        if (memberRepository.findByEmail(member.getEmail()) != null) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다: " + member.getEmail());
        }
        if (memberRepository.findByTel(member.getTel()) != null) {
            throw new IllegalArgumentException("이미 가입된 전화번호입니다: " + member.getTel());
        }
    }

    // Spring Security의 UserDetailsService 구현

    // 로그인한 사용자 정보를 가져오는 메서드
    public Member getLoggedInMember() {
        // SecurityContextHolder에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("로그인한 사용자가 없습니다.");
        }

        // Principal 객체에서 이메일(또는 username) 가져오기
        String email = authentication.getName();

        // 이메일로 Member 조회
        return memberRepository.findByEmail(email);
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다. : " + email);
        }
        // 로그좀 찍어봄
        System.out.println("Role for memeber : "+ member.getRole().name());

        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword()) // 암호화된 비밀번호
                .roles(member.getRole().toString())

                .build();
    }


    // 이메일로 사용자 조회
    public Member findByEmail(String email) {
        System.out.println("Searching for user with email : " + email);
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return member;
    }

    // 관리자 조회
    public Member findAdmin() {
        return memberRepository.findByRole(Role.ADMIN)
                .orElseThrow(() -> new IllegalStateException("Admin not found"));
    }
}