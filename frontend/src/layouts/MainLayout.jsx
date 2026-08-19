/**
 * 사용 예시:
 * <MainLayout>
 *   <ProjectListPage />
 * </MainLayout>
 *
 * 로그인 사용자 정보(useUser)를 자동으로 읽어 사이드바 하단에 표시합니다.
 * 비로그인 상태이거나 아직 불러오는 중이면 해당 영역은 표시되지 않습니다.
 */

import { Link, useLocation } from "react-router-dom";
import Sidebar from "../components/Sidebar.jsx";
import { useUser } from "../contexts/UserContext.jsx";

const mobileMenus = [
  { label: "홈", path: "/home" },
  { label: "프로젝트", path: "/projects" },
  { label: "메시지", path: "/notes" },
  { label: "설정", path: "/settings" },
];

export default function MainLayout({ children }) {
  const location = useLocation();
  const { user } = useUser();
  const sidebarUser = user ? { name: user.name, role: user.duty } : null;

  return (
    <div className="min-h-screen bg-secondary md:flex md:h-screen">
      <Sidebar user={sidebarUser} />
      <header className="sticky top-0 z-20 flex items-center justify-between bg-primary-dark px-5 py-4 text-accent md:hidden">
        <Link to="/home" className="text-lg font-bold">MORROW.</Link>
        <nav className="flex gap-1">
          {mobileMenus.map((menu) => (
            <Link
              key={menu.path}
              to={menu.path}
              className={`rounded px-2 py-1 text-xs ${
                location.pathname === menu.path ||
                location.pathname.startsWith(`${menu.path}/`)
                  ? "bg-active text-white"
                  : "text-accent/70"
              }`}
            >
              {menu.label}
            </Link>
          ))}
        </nav>
      </header>
      <main className="min-h-0 min-w-0 flex-1 overflow-y-auto p-5 md:p-8">{children}</main>
    </div>
  );
}
