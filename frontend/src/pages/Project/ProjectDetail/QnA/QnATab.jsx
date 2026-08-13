/**
 * 프로젝트 상세 > 컨텍스트 Q&A 탭
 *
 * 사용 예시:
 * <QnATab />
 *
 * 현재 답변은 mock이며, 실제 연동 시 POST /projects/{projectId}/qna 응답으로 교체합니다.
 */

import { useState } from "react";
import Avatar from "../../../../components/Avatar.jsx";
import Button from "../../../../components/Button.jsx";
import Input from "../../../../components/Input.jsx";
import { qnaHistory } from "../../../../api/mock/projectDetailData.js";

const MOCK_ANSWER =
  "아직 실제 AI 응답이 연동되지 않았습니다. 연동 후 프로젝트 맥락을 바탕으로 답변합니다.";

export default function QnATab() {
  const [history, setHistory] = useState(qnaHistory);
  const [question, setQuestion] = useState("");

  const handleSubmit = (event) => {
    event.preventDefault();
    const trimmed = question.trim();
    if (!trimmed) return;

    setHistory((prev) => [
      ...prev,
      { question: trimmed, answer: MOCK_ANSWER, sources: [], createdAt: null },
    ]);
    setQuestion("");
  };

  return (
    <div>
      <div className="space-y-6">
        {history.map((item, index) => (
          <div key={index}>
            <p className="ml-auto w-fit max-w-lg rounded-lg bg-secondary px-4 py-2 text-sm text-primary">
              {item.question}
            </p>

            <div className="mt-4 flex gap-3">
              <Avatar name="AI 팀원" size="md" />
              <div className="min-w-0 flex-1">
                <p className="text-sm font-bold text-primary">
                  AI 팀원
                  <span className="ml-2 text-xs font-normal text-muted">
                    방금 전
                  </span>
                </p>
                <p className="mt-2 text-sm leading-6 text-muted">{item.answer}</p>

                {item.sources.length > 0 && (
                  <ul className="mt-3 space-y-2">
                    {item.sources.map((source) => (
                      <li key={source.documentId}>
                        <a
                          href={source.sourceUrl}
                          className="flex items-center justify-between rounded-lg border border-border px-4 py-2.5 text-xs text-muted hover:bg-secondary"
                        >
                          <span>
                            {source.occurredAt} · {source.title}
                          </span>
                          <span aria-hidden="true">›</span>
                        </a>
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>

      <form onSubmit={handleSubmit} className="mt-8 flex gap-2">
        <Input
          value={question}
          onChange={(event) => setQuestion(event.target.value)}
          placeholder="이 프로젝트에 대해 질문하세요"
        />
        <Button type="submit">전송</Button>
      </form>
    </div>
  );
}
