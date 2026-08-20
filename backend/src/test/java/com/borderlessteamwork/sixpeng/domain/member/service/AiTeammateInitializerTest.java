package com.borderlessteamwork.sixpeng.domain.member.service;

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
class AiTeammateInitializerTest {

    @Autowired
    private AiTeammateInitializer initializer;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("AI 팀원이 없으면 예약된 값으로 생성한다")
    void createsAiTeammateWhenMissing() {
        memberRepository.findByGoogleId(Member.AI_TEAMMATE_GOOGLE_ID)
                .ifPresent(memberRepository::delete);
        memberRepository.flush();

        initializer.run(null);

        Member ai = memberRepository.findByGoogleId(Member.AI_TEAMMATE_GOOGLE_ID).orElseThrow();
        assertThat(ai.getEmail()).isEqualTo("ai@sixpeng.internal");
        assertThat(ai.getName()).isEqualTo("AI 팀원");
        assertThat(ai.isAiTeammate()).isTrue();
    }

    @Test
    @DisplayName("이미 있으면 중복 생성하지 않는다")
    void doesNotDuplicateExistingAiTeammate() {
        initializer.run(null);
        Long firstId = memberRepository.findByGoogleId(Member.AI_TEAMMATE_GOOGLE_ID).orElseThrow().getId();

        initializer.run(null);

        Member ai = memberRepository.findByGoogleId(Member.AI_TEAMMATE_GOOGLE_ID).orElseThrow();
        assertThat(ai.getId()).isEqualTo(firstId);
    }

    @Test
    @DisplayName("다른 도메인은 MemberService.findAiTeammate() 로 AI 팀원을 찾는다")
    void findAiTeammateExposesSeededAccount() {
        initializer.run(null);

        Member ai = memberService.findAiTeammate();

        assertThat(ai.getGoogleId()).isEqualTo(Member.AI_TEAMMATE_GOOGLE_ID);
        assertThat(ai.isAiTeammate()).isTrue();
    }

    @Test
    @DisplayName("일반 회원은 AI 팀원이 아니다")
    void normalMemberIsNotAiTeammate() {
        Member normal = memberRepository.save(
                Member.ofGoogle("g-normal", "normal@example.com", "일반",
                        com.borderlessteamwork.sixpeng.domain.member.entity.Language.KR));

        assertThat(normal.isAiTeammate()).isFalse();
    }
}
