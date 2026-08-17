/**
 * 디자인 확정 시 교체 필요: 색상 값은 tailwind.config.js의 임시 토큰 기준입니다.
 *
 * 사용 예시 (내부 상태로 동작):
 * <Tab
 *   tabs={[
 *     { label: "홈", content: <ProjectHome /> },
 *     { label: "AI 브리핑 상세", content: <Briefing /> },
 *     { label: "컨텍스트 Q&A", content: <QnA /> },
 *     { label: "팀원 관리", content: <Members /> },
 *     { label: "연동 상태", content: <Integration /> },
 *   ]}
 * />
 *
 * 사용 예시 (외부에서 탭을 전환해야 할 때, 예: 버튼 클릭으로 다른 탭 이동):
 * const [activeIndex, setActiveIndex] = useState(0);
 * <Tab tabs={tabs} activeIndex={activeIndex} onTabChange={setActiveIndex} />
 */

import { useState } from "react";

export default function Tab({ tabs, activeIndex: controlledIndex, onTabChange }) {
  const [internalIndex, setInternalIndex] = useState(0);
  const isControlled = controlledIndex !== undefined;
  const activeIndex = isControlled ? controlledIndex : internalIndex;

  const selectTab = (index) => {
    if (isControlled) {
      onTabChange?.(index);
    } else {
      setInternalIndex(index);
    }
  };

  return (
    <div>
      <div className="flex gap-4 border-b border-border">
        {tabs.map((tab, index) => (
          <button
            key={tab.label}
            type="button"
            onClick={() => selectTab(index)}
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
