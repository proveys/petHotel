package com.shop.repository;

import com.shop.constant.Role;
import com.shop.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Member findByEmail(String email);
    Member findByTel(String tel);
    Optional<Member> findByRole(Role role);
}
