/**
 * 프로젝트 상세 > AI 브리핑 상세 탭
 *
 * 사용 예시:
 * <BriefingTab projectId={project.id} onAskQuestion={() => ...} />
 *
 * onAskQuestion: "Q&A로 질문하기" 클릭 시 실행 (컨텍스트 Q&A 탭으로 이동)
 *
 * 명세의 Briefing에는 title/impact가 없어(summary만 존재) 해당 UI는
 * 값이 있을 때만 표시되도록 가드했습니다.
 */

import { useEffect, useState } from "react";
import Badge from "../../../../components/Badge.jsx";
import Button from "../../../../components/Button.jsx";
import { getTodayBriefing } from "../../../../api/briefing.js";

const impactLabel = { HIGH: "영향도 높음", MEDIUM: "영향도 보통", LOW: "영향도 낮음" };
const impactVariant = { HIGH: "danger", MEDIUM: "default", LOW: "success" };

export default function BriefingTab({ projectId, onAskQuestion }) {
  const [briefing, setBriefing] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let ignore = false;

    getTodayBriefing(projectId)
      .then(({ data }) => {
        if (!ignore) setBriefing(data);
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

  if (loading) return <p className="text-sm text-muted">불러오는 중...</p>;
  if (error) return <p className="text-sm text-danger">브리핑을 불러오지 못했어요.</p>;
  if (!briefing) return <p className="text-sm text-muted">아직 오늘의 브리핑이 없어요.</p>;

  return (
    <div className="space-y-4">
      <article className="rounded-xl border border-border bg-white p-5">
        <div className="flex items-start justify-between gap-3">
          <h3 className="font-bold text-primary">{briefing.title ?? "오늘의 브리핑"}</h3>
          {briefing.impact && (
            <Badge variant={impactVariant[briefing.impact]}>
              {impactLabel[briefing.impact]}
            </Badge>
          )}
        </div>

        <p className="mt-3 text-sm leading-6 text-muted">{briefing.summary}</p>

        {briefing.sources?.length > 0 && (
          <ul className="mt-4 space-y-2">
            {briefing.sources.map((source) => (
              <li key={source.documentId}>
                <a
                  href={source.sourceUrl}
                  className="flex items-center justify-between rounded-lg border border-border px-4 py-3 text-xs text-muted hover:bg-secondary"
                >
                  <span>{source.title}</span>
                  <span aria-hidden="true">›</span>
                </a>
              </li>
            ))}
          </ul>
        )}

        <Button
          variant="secondary"
          className="mt-4"
          onClick={() => onAskQuestion?.(briefing)}
        >
          Q&A로 질문하기
        </Button>
      </article>
    </div>
  );
}
