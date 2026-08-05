/**
 * 디자인 확정 시 교체 필요: 색상 값은 tailwind.config.js의 임시 토큰 기준입니다.
 *
 * 사용 예시:
 * <Tab
 *   tabs={[
 *     { label: "홈", content: <ProjectHome /> },
 *     { label: "AI 브리핑 상세", content: <Briefing /> },
 *     { label: "컨텍스트 Q&A", content: <QnA /> },
 *     { label: "팀원 관리", content: <Members /> },
 *     { label: "연동 상태", content: <Integration /> },
 *   ]}
 * />
 */

import { useState } from "react";

export default function Tab({ tabs }) {
  const [activeIndex, setActiveIndex] = useState(0);

  return (
    <div>
      <div className="flex gap-4 border-b border-border">
        {tabs.map((tab, index) => (
          <button
            key={tab.label}
            type="button"
            onClick={() => setActiveIndex(index)}
            className={`-mb-px border-b-2 px-1 py-2 text-sm ${
              index === activeIndex
                ? "border-primary font-medium text-primary"
                : "border-transparent text-muted hover:text-primary"
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>
      <div className="pt-4">{tabs[activeIndex]?.content}</div>
    </div>
  );
}
