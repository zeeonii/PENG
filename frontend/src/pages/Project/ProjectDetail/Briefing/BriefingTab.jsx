/**
 * 프로젝트 상세 > AI 브리핑 상세 탭
 *
 * 사용 예시:
 * <BriefingTab onAskQuestion={(briefing) => ...} />
 *
 * onAskQuestion: "Q&A로 질문하기" 클릭 시 실행 (컨텍스트 Q&A 탭으로 이동)
 */

import Badge from "../../../../components/Badge.jsx";
import Button from "../../../../components/Button.jsx";
import {
  briefings,
  impactLabel,
  impactVariant,
} from "../../../../api/mock/projectDetailData.js";

export default function BriefingTab({ onAskQuestion }) {
  return (
    <div className="space-y-4">
      {briefings.map((briefing) => (
        <article
          key={briefing.id}
          className="rounded-xl border border-border bg-white p-5"
        >
          <div className="flex items-start justify-between gap-3">
            <h3 className="font-bold text-primary">{briefing.title}</h3>
            <Badge variant={impactVariant[briefing.impact]}>
              {impactLabel[briefing.impact]}
            </Badge>
          </div>

          <p className="mt-3 text-sm leading-6 text-muted">{briefing.summary}</p>

          <ul className="mt-4 space-y-2">
            {briefing.sources.map((source) => (
              <li key={source.documentId}>
                <a
                  href={source.sourceUrl}
                  className="flex items-center justify-between rounded-lg border border-border px-4 py-3 text-xs text-muted hover:bg-secondary"
                >
                  <span>
                    {source.occurredAt} · {source.title}
                  </span>
                  <span aria-hidden="true">›</span>
                </a>
              </li>
            ))}
          </ul>

          <Button
            variant="secondary"
            className="mt-4"
            onClick={() => onAskQuestion?.(briefing)}
          >
            Q&A로 질문하기
          </Button>
        </article>
      ))}
    </div>
  );
}
