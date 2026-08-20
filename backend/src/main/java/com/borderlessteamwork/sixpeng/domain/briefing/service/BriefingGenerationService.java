package com.borderlessteamwork.sixpeng.domain.briefing.service;

import com.borderlessteamwork.sixpeng.domain.briefing.entity.Briefing;
import com.borderlessteamwork.sixpeng.domain.briefing.entity.BriefingSource;
import com.borderlessteamwork.sixpeng.domain.briefing.repository.BriefingRepository;
import com.borderlessteamwork.sixpeng.domain.briefing.repository.BriefingSourceRepository;
import com.borderlessteamwork.sixpeng.domain.document.entity.Document;
import com.borderlessteamwork.sixpeng.domain.document.repository.DocumentRepository;
import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.domain.member.repository.MemberRepository;
import com.borderlessteamwork.sixpeng.domain.project.entity.Project;
import com.borderlessteamwork.sixpeng.domain.project.repository.ProjectRepository;
import com.borderlessteamwork.sixpeng.domain.qna.service.OpenAiService;
import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BriefingGenerationService {

    private static final Pattern RELEVANT_DOCS_PATTERN = Pattern.compile("\\[관련 문서:\\s*([0-9,\\s]+)]");

    private final BriefingRepository briefingRepository;
    private final BriefingSourceRepository briefingSourceRepository;
    private final DocumentRepository documentRepository;
    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;
    private final OpenAiService openAiService;

    public void generateTodayBriefingIfNeeded(Long projectId, Long memberId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "Project not found: id=" + projectId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "Member not found: id=" + memberId));

        LocalDateTime since = briefingRepository
                .findFirstByProjectIdAndMemberIdOrderByCreatedAtDesc(projectId, memberId)
                .map(Briefing::getCreatedAt)
                .orElse(LocalDateTime.now().minusDays(1));

        List<Document> newDocuments = documentRepository
                .findByProjectIdAndCollectedAtAfterOrderByCollectedAtAsc(projectId, since);

        if (newDocuments.isEmpty()) {
            log.info("새로 쌓인 문서가 없어 브리핑을 생성하지 않음: projectId={}, memberId={}", projectId, memberId);
            return;
        }

        BriefingDraft draft = summarizeForMember(member, newDocuments);
        if (draft == null) {
            // 실제 브리핑은 아니지만, 이 문서 묶음을 검토했다는 사실 자체는 저장해
            // "마지막으로 검토한 시점" 커서를 앞으로 옮긴다. 그러지 않으면 같은
            // (무관한) 문서 묶음을 조회할 때마다 매번 다시 OpenAI에 보내게 된다.
            Briefing noUpdate = Briefing.builder()
                    .project(project)
                    .member(member)
                    .summary("")
                    .build();
            noUpdate.markNoUpdate();
            briefingRepository.save(noUpdate);
            log.info("담당 업무와 관련된 변경사항이 없어 브리핑을 생성하지 않음: projectId={}, memberId={}", projectId, memberId);
            return;
        }

        Briefing briefing = Briefing.builder()
                .project(project)
                .member(member)
                .summary(draft.summary())
                .build();
        briefingRepository.save(briefing);

        for (Document document : draft.relevantDocuments()) {
            briefingSourceRepository.save(BriefingSource.builder()
                    .briefing(briefing)
                    .document(document)
                    .build());
        }

        log.info("브리핑 생성 완료: projectId={}, memberId={}, briefingId={}, sourceCount={}",
                projectId, memberId, briefing.getId(), draft.relevantDocuments().size());
    }

    private BriefingDraft summarizeForMember(Member member, List<Document> documents) {
        String duty = Optional.ofNullable(member.getDuty()).filter(d -> !d.isBlank()).orElse("전체 업무");

        StringBuilder documentsBlock = new StringBuilder();
        for (int i = 0; i < documents.size(); i++) {
            Document d = documents.get(i);
            documentsBlock.append("[문서 %d] (id=%d, 출처=%s)\n%s\n\n"
                    .formatted(i + 1, d.getId(), d.getSourceType(), truncate(d.getContent(), 1000)));
        }

        String systemPrompt = """
                        당신은 팀의 변경사항을 팀원에게 브리핑하는 AI 팀원입니다.
                        주어진 문서 목록을 검토해, 이 팀원의 담당 업무와 조금이라도 관련이
                        있을 수 있는 문서를 모두 포함해 2~3문장으로 간결하게 한국어로 요약하세요.
                        API 변경, 코드 구조 변경, 배포/인프라 관련 내용은 백엔드 담당자와
                        관련이 있다고 간주하세요.
                        정말로 업무와 전혀 무관한 내용(예: 순수 디자인 시안, 마케팅 문구)만
                        있을 때만 "RELEVANT_NONE"이라고 답하세요.
                        답변 마지막 줄에 관련 문서 번호를 "[관련 문서: 1, 3]" 형식으로 표시하세요.
                        """;

        String userPrompt = """
                [담당 업무]
                %s

                [새로 쌓인 문서]
                %s
                """.formatted(duty, documentsBlock);

        String rawAnswer = openAiService.chatCompletion(systemPrompt, userPrompt);
        log.info("LLM 브리핑 판단 결과: documentsCount={}, rawAnswer={}", documents.size(), rawAnswer);

        if (rawAnswer.contains("RELEVANT_NONE")) {
            return null;
        }

        List<Document> relevant = parseRelevantDocuments(rawAnswer, documents);
        if (relevant.isEmpty()) {
            relevant = documents;
        }

        String summary = rawAnswer.replaceAll("\\[관련 문서:.*?]", "").trim();
        return new BriefingDraft(summary, relevant);
    }

    private List<Document> parseRelevantDocuments(String answer, List<Document> documents) {
        Matcher matcher = RELEVANT_DOCS_PATTERN.matcher(answer);
        if (!matcher.find()) {
            return List.of();
        }
        return Arrays.stream(matcher.group(1).split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .filter(idx -> idx >= 1 && idx <= documents.size())
                .map(idx -> documents.get(idx - 1))
                .distinct()
                .toList();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength) + "...";
    }

    private record BriefingDraft(String summary, List<Document> relevantDocuments) {
    }
}
