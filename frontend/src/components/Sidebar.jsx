import { Link, useLocation } from "react-router-dom";

const menuItems = [
  { label: "홈", path: "/home" },
  { label: "프로젝트", path: "/projects" },
  { label: "메시지", path: "/notes" },
];

export default function Sidebar() {
  const location = useLocation();

  return (
    <aside className="hidden h-screen w-56 shrink-0 flex-col bg-primary-dark px-4 py-6 text-accent md:flex">
      <Link to="/home" className="mb-1 text-2xl font-bold tracking-tight">
        REMI<span className="text-accent">.</span>
      </Link>
      <p className="mb-7 flex items-center gap-1.5 text-xs text-accent/80">
        <span className="h-1.5 w-1.5 rounded-full bg-accent" />레미 온라인
      </p>
      <nav className="flex flex-col gap-1">
        {menuItems.map((item) => {
          const active =
            location.pathname === item.path ||
            (item.path === "/projects" && location.pathname.startsWith("/projects"));
          return (
            <Link
              key={item.path}
              to={item.path}
              className={`rounded-md px-3 py-2 text-sm transition-colors ${
                active
                  ? "bg-primary text-accent font-semibold"
                  : "text-accent/75 hover:bg-white/10 hover:text-accent"
              }`}
            >
              {item.label}
            </Link>
          );
        })}
      </nav>
      <div className="mt-auto border-t border-white/15 pt-4 text-xs text-accent/70">
        <strong className="block text-accent">김승언</strong>
        <span>프론트엔드 개발</span>
      </div>
    </aside>
  );
}
