package com.borderlessteamwork.sixpeng.domain.qna.service;

import com.borderlessteamwork.sixpeng.domain.document.entity.Document;
import com.borderlessteamwork.sixpeng.domain.document.repository.DocumentRepository;
import com.borderlessteamwork.sixpeng.domain.member.entity.Member;
import com.borderlessteamwork.sixpeng.domain.member.repository.MemberRepository;
import com.borderlessteamwork.sixpeng.domain.project.entity.Project;
import com.borderlessteamwork.sixpeng.domain.project.repository.ProjectRepository;
import com.borderlessteamwork.sixpeng.domain.qna.dto.response.QnaHistoryPageResponse;
import com.borderlessteamwork.sixpeng.domain.qna.dto.response.QnaResponse;
import com.borderlessteamwork.sixpeng.domain.qna.entity.QnaHistory;
import com.borderlessteamwork.sixpeng.domain.qna.entity.QnaSource;
import com.borderlessteamwork.sixpeng.domain.qna.repository.QnaHistoryRepository;
import com.borderlessteamwork.sixpeng.domain.qna.repository.QnaSourceRepository;
import com.borderlessteamwork.sixpeng.global.exception.BusinessException;
import com.borderlessteamwork.sixpeng.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QnaService {

    private static final int TOP_K = 5;

    private final ChromaService chromaService;
    private final OpenAiService openAiService;
    private final QnaHistoryRepository qnaHistoryRepository;
    private final QnaSourceRepository qnaSourceRepository;
    private final DocumentRepository documentRepository;
    private final ProjectRepository projectRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public QnaResponse ask(Long projectId, Long memberId, String question) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "Project not found: id=" + projectId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "Member not found: id=" + memberId));

        List<Double> questionEmbedding = openAiService.createEmbedding(question);

        List<ChromaService.ChromaQueryResult> results = chromaService.query(projectId, questionEmbedding, TOP_K);

        List<String> contextChunks = results.stream()
                .map(ChromaService.ChromaQueryResult::chunkText)
                .toList();

        String answer = openAiService.generateAnswer(question, contextChunks);

        QnaHistory qnaHistory = QnaHistory.builder()
                .project(project)
                .member(member)
                .question(question)
                .answer(answer)
                .build();
        qnaHistoryRepository.save(qnaHistory);

        Map<Long, Document> uniqueDocuments = new LinkedHashMap<>();
        for (ChromaService.ChromaQueryResult result : results) {
            if (!uniqueDocuments.containsKey(result.documentId())) {
                documentRepository.findById(result.documentId())
                        .ifPresent(doc -> uniqueDocuments.put(result.documentId(), doc));
            }
        }

        List<QnaResponse.SourceItem> sourceItems = uniqueDocuments.values().stream()
                .map(doc -> {
                    qnaSourceRepository.save(QnaSource.builder()
                            .qnaHistory(qnaHistory)
                            .document(doc)
                            .build());
                    return toSourceItem(doc);
                })
                .toList();

        return QnaResponse.of(qnaHistory, sourceItems);
    }

    public QnaHistoryPageResponse getHistory(Long projectId, int page, int size) {
        Page<QnaHistory> historyPage = qnaHistoryRepository.findByProjectIdOrderByCreatedAtDesc(
                projectId, PageRequest.of(page, size));

        List<QnaResponse> content = historyPage.getContent().stream()
                .map(history -> {
                    List<QnaResponse.SourceItem> sources = qnaSourceRepository.findByQnaHistoryId(history.getId()).stream()
                            .map(qs -> toSourceItem(qs.getDocument()))
                            .toList();
                    return QnaResponse.of(history, sources);
                })
                .toList();

        return QnaHistoryPageResponse.builder()
                .content(content)
                .hasNext(historyPage.hasNext())
                .build();
    }

    private QnaResponse.SourceItem toSourceItem(Document document) {
        return QnaResponse.SourceItem.builder()
                .documentId(document.getId())
                .title(document.getTitle())
                .sourceType(document.getSourceType().name())
                .sourceUrl(document.getSourceUrl())
                .build();
    }
}
