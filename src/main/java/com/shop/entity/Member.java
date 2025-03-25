package com.shop.entity;

import com.shop.constant.Role;
import com.shop.dto.MemberFormDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "member")
@Getter
@Setter
@ToString
public class Member extends BaseEntity implements UserDetails {
    //기본키 컬럼명 = member_id AI --> 데이터 저장시 1씩 증가
    @Id // 기본 키 임을 나타냄
    @Column(name="member_id") // 데이터베이스 테이블에서 매핑되는 컬럼 이름을 지정
    // 여기서 기본 키 id 필드가 member_id라는 컬럼 으로 매핑 되버림.
    @GeneratedValue(strategy = GenerationType.AUTO) // 기본 키 값을 자동으로 생성함.
    private Long id;
    //알아서 설정
    private String name;
    //중복 허용 x
    @Column(unique = true) // Email 필드에 유니크 제약 조건을 걸어 중복을 방지함.
    private String email;
    //알아서
    private String password;
    //알아서
    private String address;
    private String detailAddress;
    private String tel;
    //Enum --> 컴: 숫자  우리 : 문자
    //데이터베이스 문자 그대로 --> USER, ADMIN
    @Enumerated(EnumType.STRING) //role 필드를 enum타입으로 정의하고, 이를 데이터베이스에 문자열 형태로 저장
    // Enumtype.String 은 값 그대로 저장하며, 숫자 대신 가독성이 높은 문자열(USER, ADMIN)로 저장됨/
    private Role role;

    @Override
    public String getPassword() {
        return this.password; // 비밀번호
    }



    @Override
    public String getUsername() {
        return this.email; // 이메일을 유저네임으로 사용
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // role이 ADMIN이면 ROLE_ADMIN을 반환
        if (this.role == Role.ADMIN) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    public static Member createMember(MemberFormDto memberFormDto,
                                      PasswordEncoder passwordEncoder) {
        //PasswordEncoder 를 통해 비밀번호가 암호화 됨/
        Member member = new Member(); // 새로운 Member 객체 생성/
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setAddress(memberFormDto.getAddress());
        String password = passwordEncoder.encode(memberFormDto.getPassword()); // 비밀번호 암호화
        member.setPassword(password);
        member.setTel(memberFormDto.getTel());
        member.setRole(Role.ADMIN); // 기본 역할 ADMIN 설정
        return member;
    }
}
