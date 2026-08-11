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
     * AI 팀원 계정을 가져온다. 애플리케이션 시작 시 시딩되므로 없으면 초기화 실패다.
     * qna, briefing 등 'AI 가 한 일'을 회원으로 표시해야 하는 도메인에서 사용한다.
     */
    public Member findAiTeammate() {
        return memberRepository.findByGoogleId(Member.AI_TEAMMATE_GOOGLE_ID)
                .orElseThrow(() -> new BusinessException(ErrorCode.AI_TEAMMATE_NOT_INITIALIZED));
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

        // 생략한 필드는 기존 값을 유지하므로, 적용 후의 최종 상태로 검증해야 한다.
        boolean showOriginalText = request.showOriginalText() != null
                ? request.showOriginalText() : member.isShowOriginalText();
        boolean showTranslatedText = request.showTranslatedText() != null
                ? request.showTranslatedText() : member.isShowTranslatedText();
        if (!showOriginalText && !showTranslatedText) {
            throw new BusinessException(ErrorCode.INVALID_TRANSLATION_DISPLAY_SETTING);
        }

        member.updateProfile(
                request.language(),
                request.country(),
                request.timezone(),
                request.duty(),
                showOriginalText,
                showTranslatedText
        );
        return member;
    }
}
