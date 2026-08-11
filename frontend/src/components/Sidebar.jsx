import { Link, useLocation } from "react-router-dom";

const menuItems = [
  { label: "홈", path: "/home" },
  { label: "프로젝트", path: "/projects" },
  { label: "쪽지", path: "/notes" },
];

export default function Sidebar() {
  const location = useLocation();

  return (
    <aside className="hidden h-screen w-56 shrink-0 flex-col border-r border-border bg-white px-4 py-6 md:flex">
      <Link to="/home" className="mb-1 text-xl font-bold tracking-tight text-primary">teamline<span className="text-active">.</span></Link>
      <p className="mb-7 flex items-center gap-1.5 text-xs text-muted"><span className="h-1.5 w-1.5 rounded-full bg-success" />AI 팀원 온라인</p>
      <nav className="flex flex-col gap-1">
        {menuItems.map((item) => {
          const active = location.pathname === item.path || (item.path === "/projects" && location.pathname.startsWith("/projects"));
          return <Link key={item.path} to={item.path} className={`rounded-md px-3 py-2 text-sm ${active ? "bg-secondary font-semibold text-primary" : "text-muted hover:bg-secondary"}`}>{item.label}</Link>;
        })}
      </nav>
      <div className="mt-auto border-t border-border pt-4 text-xs text-muted"><strong className="block text-primary">김승언</strong><span>프론트엔드 개발</span></div>
    </aside>
  );
}
