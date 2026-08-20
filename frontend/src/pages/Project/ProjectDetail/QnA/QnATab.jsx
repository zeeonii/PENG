/**
 * 프로젝트 상세 > 컨텍스트 Q&A 탭
 *
 * 사용 예시:
 * <QnATab projectId={project.id} />
 */

import { useEffect, useRef, useState } from "react";
import Avatar from "../../../../components/Avatar.jsx";
import Button from "../../../../components/Button.jsx";
import Input from "../../../../components/Input.jsx";
import { askQuestion, getQnaHistory } from "../../../../api/qna.js";

export default function QnATab({ projectId }) {
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [question, setQuestion] = useState("");
  const [asking, setAsking] = useState(false);
  const historyEndRef = useRef(null);

  useEffect(() => {
    let ignore = false;

    getQnaHistory(projectId)
      .then(({ data }) => {
        if (ignore) return;
        // 응답이 ApiResponse<QnaHistoryPageResponse>로 감싸져 있어
        // 실제 목록은 data.data.content에 들어있다.
        // 백엔드는 최신순(내림차순)으로 내려주는데, 채팅처럼 오래된 게 위·최신이
        // 아래로 오도록 뒤집는다 (질문 전송 직후 로컬 추가와 방향을 맞추기 위함).
        setHistory([...(data.data?.content ?? [])].reverse());
      })
      .catch((err) => {
        if (!ignore) setError(err);
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, [projectId]);

  useEffect(() => {
    historyEndRef.current?.scrollIntoView({ block: "end" });
  }, [history]);

  const handleSubmit = async (event) => {
    event.preventDefault();
    const trimmed = question.trim();
    if (!trimmed || asking) return;

    setAsking(true);
    try {
      const { data } = await askQuestion(projectId, { question: trimmed });
      // 응답이 ApiResponse<QnaResponse>로 감싸져 있어 실제 항목은 data.data에 들어있다.
      setHistory((prev) => [...prev, data.data]);
      setQuestion("");
    } catch {
      setError(new Error("질문 전송에 실패했어요."));
    } finally {
      setAsking(false);
    }
  };

  return (
    <div className="flex h-[560px] flex-col">
      {error && <p className="mb-4 shrink-0 text-sm text-danger">{error.message ?? "문제가 발생했어요."}</p>}

      <div className="flex-1 space-y-6 overflow-y-auto pr-1">
        {loading && <p className="text-sm text-muted">불러오는 중...</p>}
        {!loading && (
          <>
          {history.length === 0 && (
            <p className="text-sm text-muted">아직 질문이 없어요. 궁금한 걸 물어보세요!</p>
          )}
          {history.map((item, index) => (
            <div key={index}>
              <p className="ml-auto w-fit max-w-lg rounded-lg bg-secondary px-4 py-2 text-sm text-primary">
                {item.question}
              </p>

              <div className="mt-4 flex gap-3">
                <Avatar name="REMI" size="md" />
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-bold text-primary">REMI</p>
                  <p className="mt-2 text-sm leading-6 text-muted">{item.answer}</p>

                  {item.sources?.length > 0 && (
                    <ul className="mt-3 space-y-2">
                      {item.sources.map((source) => (
                        <li key={source.documentId}>
                          <a
                            href={source.sourceUrl}
                            className="flex items-center justify-between rounded-lg border border-border px-4 py-2.5 text-xs text-muted hover:bg-secondary"
                          >
                            <span>{source.title}</span>
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
          </>
        )}
        <div ref={historyEndRef} />
      </div>

      <form onSubmit={handleSubmit} className="mt-4 flex shrink-0 gap-2 border-t border-border pt-4">
        <Input
          value={question}
          onChange={(event) => setQuestion(event.target.value)}
          placeholder="이 프로젝트에 대해 질문하세요"
          disabled={asking}
        />
        <Button type="submit" disabled={asking}>
          {asking ? "전송 중..." : "전송"}
        </Button>
      </form>
    </div>
  );
}
