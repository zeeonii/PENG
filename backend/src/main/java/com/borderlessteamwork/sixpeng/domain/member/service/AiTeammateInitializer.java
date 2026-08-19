package com.borderlessteamwork.sixpeng.domain.member.service;

import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI 팀원 계정을 시작 시 한 번 심는다. Google 로그인으로는 만들어질 수 없는 계정이라
 * 애플리케이션이 직접 만들어 두어야 프로젝트 생성 시 자동 합류가 가능하다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiTeammateInitializer implements ApplicationRunner {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        memberRepository.findByGoogleId(Member.AI_TEAMMATE_GOOGLE_ID)
                .ifPresentOrElse(
                        member -> log.debug("AI 팀원 계정 확인 (id={})", member.getId()),
                        () -> {
                            Member created = memberRepository.save(Member.ofAiTeammate());
                            log.info("AI 팀원 계정을 생성했습니다 (id={})", created.getId());
                        }
                );
    }
}
