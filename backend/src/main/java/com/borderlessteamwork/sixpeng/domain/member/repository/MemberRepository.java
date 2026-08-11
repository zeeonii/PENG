package com.borderlessteamwork.sixpeng.domain.member.repository;

import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByGoogleId(String googleId);

    Optional<Member> findByEmail(String email);
}
