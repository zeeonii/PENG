import { Link } from "react-router-dom";
import MainLayout from "../../layouts/MainLayout.jsx";
import Avatar from "../../components/Avatar.jsx";
import Badge from "../../components/Badge.jsx";
import { projects, statusVariant } from "../../api/mock/projectData.js";

export default function HomePage() {
  return (
    <MainLayout>
      <div className="mx-auto max-w-5xl">
        <header className="mb-8">
          <p className="text-xs font-semibold tracking-wider text-active">8월 12일 화요일</p>
          <h1 className="mt-2 text-3xl font-bold text-primary">좋은 아침이에요, 승언님</h1>
          <p className="mt-2 text-sm text-muted">밤사이 팀의 변화를 REMI가 정리했어요.</p>
        </header>

        <section className="rounded-2xl bg-primary p-6 text-white shadow-lg">
          <div className="flex flex-col justify-between gap-6 md:flex-row">
            <div>
              <p className="text-xs font-semibold tracking-wider text-accent">TODAY&apos;S MORROW BRIEFING</p>
              <h2 className="mt-3 text-2xl font-bold">로그인 정책이 변경되었습니다.</h2>
              <p className="mt-2 text-sm text-white/75">새 인증 방식이 적용되며 담당 화면 2개에 영향이 있어요.</p>
              <Link to="/projects/teamline" className="mt-5 inline-block border-b border-accent/60 pb-1 text-sm font-semibold text-accent">
                브리핑 자세히 보기 →
              </Link>
            </div>
            <div className="grid grid-cols-3 gap-5 border-t border-white/20 pt-4 text-center md:border-l md:border-t-0 md:pl-6 md:pt-0">
              <span><strong className="block text-xl">2</strong><small className="text-white/70">영향 작업</small></span>
              <span><strong className="block text-xl">3</strong><small className="text-white/70">새 회의록</small></span>
              <span><strong className="block text-xl">1</strong><small className="text-white/70">문서 변경</small></span>
            </div>
          </div>
        </section>

        <section className="mt-10">
          <div className="mb-4 flex items-end justify-between">
            <div>
              <p className="text-xs font-semibold tracking-wider text-active">MY PROJECTS</p>
              <h2 className="mt-1 text-xl font-bold text-primary">진행 중인 프로젝트</h2>
            </div>
            <Link to="/projects" className="text-sm font-semibold text-muted hover:text-primary">전체 보기 →</Link>
          </div>
          <div className="overflow-hidden rounded-xl border border-border bg-white">
            {projects.filter((project) => project.status !== "완료").slice(0, 3).map((project) => (
              <Link key={project.id} to={`/projects/${project.id}`} className="flex items-center gap-4 border-b border-border p-4 last:border-0 hover:bg-secondary">
                <span className="grid h-10 w-10 place-items-center rounded-lg bg-secondary font-bold text-active">{project.title[0]}</span>
                <span className="min-w-0 flex-1"><strong className="block text-sm text-primary">{project.title}</strong><small className="block truncate pt-1 text-xs text-muted">{project.description}</small></span>
                <Badge variant={statusVariant[project.status]}>{project.status}</Badge>
                <span className="hidden -space-x-2 sm:flex">{project.members.slice(0, 3).map((name) => <Avatar key={name} name={name} size="sm" />)}</span>
              </Link>
            ))}
          </div>
        </section>
      </div>
    </MainLayout>
  );
}
