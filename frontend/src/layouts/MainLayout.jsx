/**
 * 사용 예시:
 * <MainLayout>
 *   <ProjectListPage />
 * </MainLayout>
 */

import Sidebar from "../components/Sidebar.jsx";

export default function MainLayout({ children }) {
  return (
    <div className="flex min-h-screen bg-secondary">
      <Sidebar />
      <main className="min-w-0 flex-1 overflow-y-auto p-5 md:p-8">{children}</main>
    </div>
  );
}
