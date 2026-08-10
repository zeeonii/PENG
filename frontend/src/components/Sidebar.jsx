/**
 * 디자인 확정 시 교체 필요: 로고는 텍스트("teamline")로, AI 온라인 표시는
 * 임시 점(dot)으로 처리되어 있습니다.
 *
 * 사용 예시:
 * <Sidebar /> // MainLayout 안에서 사용, 별도 props 없음
 */

import { Link, useLocation } from "react-router-dom";

const MENU_ITEMS = [
  { label: "홈", path: "/home" },
  { label: "프로젝트", path: "/projects" },
  { label: "쪽지", path: "/notes" },
  { label: "설정", path: "/settings" },
];

export default function Sidebar({ user }) {
  const location = useLocation();

  return (
    <aside className="hidden h-screen w-56 shrink-0 flex-col border-r border-border bg-white px-4 py-6 md:flex">
      <Link
        to="/home"
        className="mb-1 text-xl font-bold tracking-tight text-primary"
      >
        teamline<span className="text-active">.</span>
      </Link>
      <p className="mb-7 flex items-center gap-1.5 text-xs text-muted">
        <span className="h-1.5 w-1.5 rounded-full bg-success" />
        AI 팀원 온라인
      </p>

      <nav className="flex flex-col gap-1">
        {MENU_ITEMS.map(({ label, path }) => {
          const isActive =
            location.pathname === path || location.pathname.startsWith(`${path}/`);

          return (
            <Link
              key={path}
              to={path}
              className={`rounded-md px-3 py-2 text-sm ${
                isActive
                  ? "bg-secondary font-semibold text-primary"
                  : "text-muted hover:bg-secondary"
              }`}
            >
              {label}
            </Link>
          );
        })}
      </nav>

      {user && (
        <div className="mt-auto border-t border-border pt-4 text-xs text-muted">
          <strong className="block text-primary">{user.name}</strong>
          <span>{user.role}</span>
        </div>
      )}
    </aside>
  );
}
