/**
 * 사용 예시:
 * <MainLayout>
 *   <ProjectListPage />
 * </MainLayout>
 *
 * 사이드바 하단에 로그인 사용자 정보를 표시하려면 user를 전달합니다.
 * <MainLayout user={{ name: "김지훈", role: "백엔드 개발" }}>...</MainLayout>
 */

import { Link, useLocation } from "react-router-dom";
import Sidebar from "../components/Sidebar.jsx";

const mobileMenus = [
  { label: "홈", path: "/home" },
  { label: "프로젝트", path: "/projects" },
  { label: "메시지", path: "/notes" },
  { label: "설정", path: "/settings" },
];

export default function MainLayout({ user, children }) {
  const location = useLocation();

  return (
    <div className="min-h-screen bg-secondary md:flex">
      <Sidebar user={user} />
      <header className="sticky top-0 z-20 flex items-center justify-between bg-primary-dark px-5 py-4 text-accent md:hidden">
        <Link to="/home" className="text-lg font-bold">REMI.</Link>
        <nav className="flex gap-1">
          {mobileMenus.map((menu) => (
            <Link
              key={menu.path}
              to={menu.path}
              className={`rounded px-2 py-1 text-xs ${
                location.pathname === menu.path ||
                location.pathname.startsWith(`${menu.path}/`)
                  ? "bg-primary text-accent"
                  : "text-accent/70"
              }`}
            >
              {menu.label}
            </Link>
          ))}
        </nav>
      </header>
      <main className="min-w-0 flex-1 overflow-y-auto p-5 md:p-8">{children}</main>
    </div>
  );
}
