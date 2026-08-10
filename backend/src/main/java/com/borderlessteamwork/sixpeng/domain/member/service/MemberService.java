package com.borderlessteamwork.sixpeng.domain.member.service;

import com.borderlessteamwork.sixpeng.domain.member.dto.request.MemberUpdateRequest;
import com.borderlessteamwork.sixpeng.domain.member.entity.Language;
import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.domain.member.repository.MemberRepository;
import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * Google 로그인 결과로 회원을 등록하거나, 이미 있으면 프로필을 갱신한다.
     * 언어는 최초 가입 시에만 locale 로 정하고, 이후에는 사용자가 수정한 값을 유지한다.
     */
    @Transactional
    public Member upsertGoogleMember(String googleId, String email, String name, Language language) {
        return memberRepository.findByGoogleId(googleId)
                .map(member -> {
                    member.syncGoogleProfile(email, name);
                    return member;
                })
                .orElseGet(() -> memberRepository.save(Member.ofGoogle(googleId, email, name, language)));
    }

    @Transactional
    public Member updateProfile(Long memberId, MemberUpdateRequest request) {
        Member member = findById(memberId);
        member.updateProfile(request.language(), request.country(), request.timezone(), request.duty());
        return member;
    }
}
