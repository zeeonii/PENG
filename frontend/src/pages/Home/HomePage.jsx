import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import MainLayout from "../../layouts/MainLayout.jsx";
import Avatar from "../../components/Avatar.jsx";
import Badge from "../../components/Badge.jsx";
import { getProjects, getProjectMembers } from "../../api/project.js";
import { getTodayBriefing } from "../../api/briefing.js";
import { useUser } from "../../contexts/UserContext.jsx";
import { memberDisplayName } from "../../utils/member.js";

const todayLabel = new Intl.DateTimeFormat("ko-KR", {
  month: "long",
  day: "numeric",
  weekday: "long",
}).format(new Date());

// 명세에 status 필드가 없어 값이 있을 때만 표시합니다.
const statusVariant = { 진행중: "active", 진행전: "default", 완료: "success" };

export default function HomePage() {
  const { user } = useUser();
  const [projects, setProjects] = useState([]);
  const [updatedProjects, setUpdatedProjects] = useState([]);
  const [briefingLoading, setBriefingLoading] = useState(true);

  useEffect(() => {
    let ignore = false;

    getProjects()
      .then(async ({ data }) => {
        const withMembers = await Promise.all(
          data.map((project) =>
            getProjectMembers(project.id)
              .then((res) => ({ ...project, members: res.data }))
              .catch(() => ({ ...project, members: [] })),
          ),
        );
        if (!ignore) setProjects(withMembers);

        // 홈 화면엔 프로젝트 개념이 없어, 오늘 브리핑이 생성된 프로젝트를 전부 모아 보여줍니다.
        // /briefings/today는 목록(ApiResponse<List<BriefingResponse>>)을 반환하며 오늘자는 최대 1건이다.
        const results = await Promise.all(
          data.map((project) =>
            getTodayBriefing(project.id)
              .then(({ data: res }) => ({ project, briefing: res.data?.[0] ?? null }))
              .catch(() => ({ project, briefing: null })),
          ),
        );
        if (!ignore) setUpdatedProjects(results.filter((result) => result.briefing));
      })
      .catch(() => {
        if (!ignore) setProjects([]);
      })
      .finally(() => {
        if (!ignore) setBriefingLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, []);

  const newMeetingCount = updatedProjects.reduce((sum, { briefing }) => sum + (briefing.newMeetingCount ?? 0), 0);
  const documentChangeCount = updatedProjects.reduce((sum, { briefing }) => sum + (briefing.documentChangeCount ?? 0), 0);

  return (
    <MainLayout>
      <div className="mx-auto max-w-5xl">
        <header className="mb-8">
          <p className="text-xs font-semibold tracking-wider text-active">{todayLabel}</p>
          <h1 className="mt-2 text-3xl font-bold text-primary">
            좋은 아침이에요{user ? `, ${user.name}님` : ""}
          </h1>
          <p className="mt-2 text-sm text-muted">밤사이 팀의 변화를 MORROW가 정리했어요.</p>
        </header>

        <section className="rounded-2xl bg-primary p-6 text-white shadow-lg">
          <div className="flex flex-col justify-between gap-6 md:flex-row">
            <div className="min-w-0">
              <p className="text-xs font-semibold tracking-wider text-accent">TODAY&apos;S MORROW BRIEFING</p>
              <h2 className="mt-3 text-base font-medium leading-relaxed text-white/90">
                {!briefingLoading && updatedProjects.length > 0 ? (
                  <>
                    {updatedProjects.map(({ project }, index) => (
                      <span key={project.id}>
                        {index > 0 && ", "}
                        <strong className="font-bold">{project.name}</strong>
                      </span>
                    ))}
                    {" 프로젝트에 업데이트가 있어요."}
                  </>
                ) : !briefingLoading ? (
                  "아직 오늘의 브리핑이 없어요."
                ) : (
                  ""
                )}
              </h2>
            </div>
            <div className="grid shrink-0 grid-cols-2 gap-6 border-t border-white/20 pt-4 text-center md:border-l md:border-t-0 md:pl-8 md:pt-0">
              <span><strong className="block text-xl">{newMeetingCount}</strong><small className="whitespace-nowrap text-white/70">새 회의록</small></span>
              <span><strong className="block text-xl">{documentChangeCount}</strong><small className="whitespace-nowrap text-white/70">문서 변경</small></span>
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
                <span className="grid h-10 w-10 place-items-center rounded-lg bg-secondary font-bold text-active">{project.name[0]}</span>
                <span className="min-w-0 flex-1">
                  <strong className="block text-sm text-primary">{project.name}</strong>
                  {project.description && (
                    <small className="block truncate pt-1 text-xs text-muted">{project.description}</small>
                  )}
                </span>
                {project.status && (
                  <Badge variant={statusVariant[project.status]}>{project.status}</Badge>
                )}
                <span className="hidden -space-x-1 sm:flex">
                  {project.members.slice(0, 3).map((member) => (
                    <span key={member.memberId} className="rounded-full bg-white ring-2 ring-white">
                      <Avatar name={memberDisplayName(member)} size="sm" />
                    </span>
                  ))}
                </span>
              </Link>
            ))}
            {projects.length === 0 && (
              <p className="p-4 text-sm text-muted">아직 참여 중인 프로젝트가 없어요.</p>
            )}
          </div>
        </section>
      </div>
    </MainLayout>
  );
}
