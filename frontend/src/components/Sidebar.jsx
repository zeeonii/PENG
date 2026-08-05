/**
 * 디자인 확정 시 교체 필요: 로고는 텍스트("teamline")로, AI 온라인 표시는
 * 임시 점(dot)으로 처리되어 있습니다.
 *
 * 사용 예시:
 * <Sidebar /> // MainLayout 안에서 사용, 별도 props 없음
 */

import { Link, useLocation } from "react-router-dom";

const MENU_ITEMS = [
  { label: "홈", path: "/" },
  { label: "프로젝트", path: "/projects" },
  { label: "쪽지", path: "/notes" },
  { label: "설정", path: "/settings" },
];

export default function Sidebar() {
  const location = useLocation();

  return (
    <aside className="flex h-full w-56 flex-col border-r border-border bg-white px-4 py-6">
      <div className="mb-6">
        <span className="text-lg font-bold text-primary">teamline</span>
        <div className="mt-1 flex items-center gap-1.5 text-xs text-muted">
          <span className="h-1.5 w-1.5 rounded-full bg-active" />
          AI 팀원 온라인
        </div>
      </div>

      <nav className="flex flex-col gap-1">
        {MENU_ITEMS.map(({ label, path }) => {
          const isActive =
            path === "/"
              ? location.pathname === "/"
              : location.pathname.startsWith(path);

          return (
            <Link
              key={path}
              to={path}
              className={`rounded-md px-3 py-2 text-sm ${
                isActive
                  ? "bg-secondary font-medium text-primary"
                  : "text-muted hover:bg-secondary"
              }`}
            >
              {label}
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}
