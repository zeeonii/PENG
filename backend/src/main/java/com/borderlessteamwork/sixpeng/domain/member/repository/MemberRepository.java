package com.borderlessteamwork.sixpeng.domain.member.repository;

import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
