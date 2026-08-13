/**
 * 설정 화면 공통 레이아웃 (좌측 서브 메뉴 + 우측 콘텐츠)
 *
 * 사용 예시:
 * <SettingsLayout>
 *   <ProfileForm />
 * </SettingsLayout>
 */

import { Link, useLocation } from "react-router-dom";
import MainLayout from "../../layouts/MainLayout.jsx";

const SUB_MENU = [
  { label: "연동 관리", path: "/settings" },
  { label: "프로필 · 언어 설정", path: "/settings/profile" },
];

export default function SettingsLayout({ children }) {
  const location = useLocation();

  return (
    <MainLayout>
      <div className="flex flex-col gap-8 md:flex-row">
        <nav className="flex shrink-0 gap-2 md:w-48 md:flex-col">
          {SUB_MENU.map(({ label, path }) => {
            const isActive = location.pathname === path;

            return (
              <Link
                key={path}
                to={path}
                className={`border-l-2 px-3 py-1.5 text-sm ${
                  isActive
                    ? "border-primary font-semibold text-primary"
                    : "border-transparent text-muted hover:text-primary"
                }`}
              >
                {label}
              </Link>
            );
          })}
        </nav>

        <div className="min-w-0 flex-1">{children}</div>
      </div>
    </MainLayout>
  );
}
