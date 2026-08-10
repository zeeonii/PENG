/**
 * 사용 예시:
 * <MainLayout>
 *   <ProjectListPage />
 * </MainLayout>
 *
 * 사이드바 하단에 로그인 사용자 정보를 표시하려면 user를 전달합니다.
 * <MainLayout user={{ name: "김지훈", role: "백엔드 개발" }}>...</MainLayout>
 */

import Sidebar from "../components/Sidebar.jsx";

export default function MainLayout({ user, children }) {
  return (
    <div className="flex min-h-screen bg-secondary">
      <Sidebar user={user} />
      <main className="min-w-0 flex-1 overflow-y-auto p-5 md:p-8">{children}</main>
    </div>
  );
}
