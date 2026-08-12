package com.borderlessteamwork.sixpeng.domain.member.service;

import com.borderlessteamwork.sixpeng.domain.member.dto.request.MemberUpdateRequest;
import com.borderlessteamwork.sixpeng.domain.member.entity.Language;
import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

// 다른 통합 테스트와 같은 애노테이션이라 스프링 컨텍스트를 공유한다.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("신규 가입 회원은 timezone 이 시스템 기본값 UTC 로 채워진다 (요구사항 5-7)")
    void newMemberGetsDefaultTimezone() {
        Member member = memberService.upsertGoogleMember(
                "g-new", "new@example.com", "신규", Language.EN);

        assertThat(member.getTimezone()).isEqualTo("UTC");
    }

    @Test
    @DisplayName("locale 을 판별하지 못하면 language 가 기본값 EN 이다 (요구사항 5-7)")
    void newMemberFallsBackToEnglish() {
        Member member = memberService.upsertGoogleMember(
                "g-unknown", "unknown@example.com", "언어미상", Language.fromLocale(null));

        assertThat(member.getLanguage()).isEqualTo(Language.EN);
    }

    @Test
    @DisplayName("한국어 계정은 KR 로 가입된다")
    void koreanMemberGetsKr() {
        Member member = memberService.upsertGoogleMember(
                "g-ko", "ko@example.com", "한국", Language.fromLocale("ko"));

        assertThat(member.getLanguage()).isEqualTo(Language.KR);
    }

    @Test
    @DisplayName("country 는 Google 이 주지 않으므로 미설정(null)으로 둔다")
    void countryStaysNullUntilUserSetsIt() {
        Member member = memberService.upsertGoogleMember(
                "g-country", "country@example.com", "국가미상", Language.EN);

        assertThat(member.getCountry()).isNull();
    }

    @Test
    @DisplayName("재로그인해도 사용자가 바꾼 timezone 을 기본값으로 되돌리지 않는다")
    void reLoginKeepsUserTimezone() {
        Member member = memberService.upsertGoogleMember(
                "g-keep", "keep@example.com", "유지", Language.EN);
        memberService.updateProfile(member.getId(),
                new MemberUpdateRequest(null, null, "Asia/Seoul", null, null, null));
        memberRepository.flush();

        memberService.upsertGoogleMember("g-keep", "keep@example.com", "유지(이름변경)", Language.EN);

        Member reloaded = memberRepository.findByGoogleId("g-keep").orElseThrow();
        assertThat(reloaded.getTimezone()).isEqualTo("Asia/Seoul");
        assertThat(reloaded.getName()).isEqualTo("유지(이름변경)");
    }

    @Test
    @DisplayName("AI 팀원도 기본 timezone 을 가진다")
    void aiTeammateAlsoHasDefaultTimezone() {
        Member ai = memberRepository.findByGoogleId(Member.AI_TEAMMATE_GOOGLE_ID)
                .orElseGet(() -> memberRepository.save(Member.ofAiTeammate()));

        assertThat(ai.getTimezone()).isEqualTo("UTC");
    }
}
