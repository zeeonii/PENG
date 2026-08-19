/**
 * 디자인 확정 시 교체 필요: 로고는 텍스트("MORROW.")로, REMI 온라인 표시는
 * 임시 점(dot)으로 처리되어 있습니다.
 *
 * 사용 예시:
 * <Sidebar />                    // 사용자 정보 없이 (하단 영역 미표시)
 * <Sidebar user={currentUser} /> // 로그인 사용자 정보 표시
 *
 * user는 { name, role } 형태이며, MainLayout을 통해 전달합니다.
 */

import { Link, useLocation, useNavigate } from "react-router-dom";
import { logout } from "../api/member.js";
import { useUser } from "../contexts/UserContext.jsx";

const MENU_ITEMS = [
  { label: "홈", path: "/home" },
  { label: "프로젝트", path: "/projects" },
  { label: "메시지", path: "/notes" },
  { label: "설정", path: "/settings" },
];

export default function Sidebar({ user }) {
  const location = useLocation();
  const navigate = useNavigate();
  const { clear } = useUser();

  const handleLogout = async () => {
    try {
      await logout();
    } catch {
      // 세션이 이미 만료된 경우에도 로컬 상태는 정리합니다.
    }
    clear();
    navigate("/login");
  };

  return (
    <aside className="hidden h-screen w-56 shrink-0 flex-col bg-primary-dark px-4 py-6 text-white md:flex">
      <Link to="/home" className="mb-1 text-2xl font-bold tracking-tight text-white">
        MORROW<span className="text-accent">.</span>
      </Link>
      <p className="mb-7 flex items-center gap-1.5 text-xs text-accent/80">
        <span className="h-1.5 w-1.5 rounded-full bg-accent" />REMI 온라인
      </p>

      <nav className="flex flex-col gap-1">
        {MENU_ITEMS.map(({ label, path }) => {
          const isActive =
            location.pathname === path || location.pathname.startsWith(`${path}/`);

          return (
            <Link
              key={path}
              to={path}
              className={`rounded-md px-3 py-2 text-sm transition-colors ${
                isActive
                  ? "bg-active font-semibold text-white"
                  : "text-white hover:bg-white/10 hover:text-accent"
              }`}
            >
              {label}
            </Link>
          );
        })}
      </nav>

      {user && (
        <div className="mt-auto flex items-center justify-between gap-2 border-t border-white/15 pt-4 text-xs text-white/70">
          <div className="min-w-0">
            <strong className="block truncate text-white">{user.name}</strong>
            <span>{user.role}</span>
          </div>
          <button
            type="button"
            onClick={handleLogout}
            className="shrink-0 rounded px-2 py-1 text-xs text-white/70 hover:bg-white/10 hover:text-white"
          >
            로그아웃
          </button>
        </div>
      )}
    </aside>
  );
}
