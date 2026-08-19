/**
 * 프로젝트 상세 > 컨텍스트 Q&A 탭
 *
 * 사용 예시:
 * <QnATab projectId={project.id} />
 */

import { useEffect, useState } from "react";
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

  useEffect(() => {
    let ignore = false;

    getQnaHistory(projectId)
      .then(({ data }) => {
        if (ignore) return;
        // 명세는 배열을 반환하지만, 페이지네이션 응답(content 배열)일 가능성도 방어합니다.
        setHistory(Array.isArray(data) ? data : (data.content ?? []));
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

  const handleSubmit = async (event) => {
    event.preventDefault();
    const trimmed = question.trim();
    if (!trimmed || asking) return;

    setAsking(true);
    try {
      const { data } = await askQuestion(projectId, { question: trimmed });
      setHistory((prev) => [...prev, data]);
      setQuestion("");
    } catch {
      setError(new Error("질문 전송에 실패했어요."));
    } finally {
      setAsking(false);
    }
  };

  return (
    <div>
      {loading && <p className="text-sm text-muted">불러오는 중...</p>}
      {error && <p className="mb-4 text-sm text-danger">{error.message ?? "문제가 발생했어요."}</p>}

      {!loading && (
        <div className="space-y-6">
          {history.length === 0 && (
            <p className="text-sm text-muted">아직 질문이 없어요. 궁금한 걸 물어보세요!</p>
          )}
          {history.map((item, index) => (
            <div key={index}>
              <p className="ml-auto w-fit max-w-lg rounded-lg bg-secondary px-4 py-2 text-sm text-primary">
                {item.question}
              </p>

              <div className="mt-4 flex gap-3">
                <Avatar name="레미" size="md" />
                <div className="min-w-0 flex-1">
                  <p className="text-sm font-bold text-primary">레미</p>
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
        </div>
      )}

      <form onSubmit={handleSubmit} className="mt-8 flex gap-2">
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
