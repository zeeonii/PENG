import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import MainLayout from "../../layouts/MainLayout.jsx";
import Avatar from "../../components/Avatar.jsx";
import Badge from "../../components/Badge.jsx";
import { getProjects } from "../../api/project.js";
import { useUser } from "../../contexts/UserContext.jsx";

// 명세의 Project에는 status/description/members가 없습니다.
// 값이 있으면 표시하고, 없으면 자연스럽게 생략되도록 안전하게 처리했습니다.
const statusVariant = { 진행중: "active", 검토중: "danger", 완료: "success" };

function formatDate(dateString) {
  if (!dateString) return "";
  return new Date(dateString).toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
}

export default function HomePage() {
  const { user } = useUser();
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let ignore = false;

    getProjects()
      .then(({ data }) => {
        if (!ignore) setProjects(data);
      })
      .catch((err) => {
        if (!ignore) setError(err);
      })
      .finally(() => {
        if (!ignore) setLoading(false);
      });

    return () => {
      ignore = true;
    };
  }, []);

  const ongoingProjects = projects
    .filter((project) => project.status !== "완료")
    .slice(0, 3);

  return (
    <MainLayout>
      <div className="mx-auto max-w-5xl">
        <header className="mb-8">
          <p className="text-xs font-semibold tracking-wider text-active">
            {formatDate(new Date().toISOString())}
          </p>
          <h1 className="mt-2 text-3xl font-bold text-primary">
            좋은 아침이에요{user?.name ? `, ${user.name}님` : ""}
          </h1>
          <p className="mt-2 text-sm text-muted">밤사이 팀의 변화를 레미가 정리했어요.</p>
        </header>

        {/*
          명세에 프로젝트를 아우르는 브리핑 조회 API가 없어(프로젝트 단위로만 존재),
          이 카드는 아직 mock 문구입니다. 실제 데이터 연결은 관련 API가 추가되면 진행합니다.
        */}
        <section className="rounded-2xl bg-primary p-6 text-white shadow-lg">
          <div className="flex flex-col justify-between gap-6 md:flex-row">
            <div>
              <p className="text-xs font-semibold tracking-wider text-accent">TODAY&apos;S REMI BRIEFING</p>
              <h2 className="mt-3 text-2xl font-bold">로그인 정책이 변경되었습니다.</h2>
              <p className="mt-2 text-sm text-white/75">새 인증 방식이 적용되며 담당 화면 2개에 영향이 있어요.</p>
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

          {loading && <p className="text-sm text-muted">불러오는 중...</p>}
          {error && (
            <p className="text-sm text-danger">프로젝트를 불러오지 못했어요.</p>
          )}

          {!loading && !error && (
            <div className="overflow-hidden rounded-xl border border-border bg-white">
              {ongoingProjects.length === 0 && (
                <p className="p-6 text-center text-sm text-muted">참여 중인 프로젝트가 없어요.</p>
              )}
              {ongoingProjects.map((project) => (
                <Link
                  key={project.id}
                  to={`/projects/${project.id}`}
                  className="flex items-center gap-4 border-b border-border p-4 last:border-0 hover:bg-secondary"
                >
                  <span className="grid h-10 w-10 place-items-center rounded-lg bg-secondary font-bold text-active">
                    {project.name?.[0]}
                  </span>
                  <span className="min-w-0 flex-1">
                    <strong className="block text-sm text-primary">{project.name}</strong>
                    <small className="block truncate pt-1 text-xs text-muted">
                      {project.description ?? `${formatDate(project.createdAt)} 생성`}
                    </small>
                  </span>
                  {project.status && (
                    <Badge variant={statusVariant[project.status]}>{project.status}</Badge>
                  )}
                  {project.members?.length > 0 && (
                    <span className="hidden -space-x-2 sm:flex">
                      {project.members.slice(0, 3).map((name) => (
                        <Avatar key={name} name={name} size="sm" />
                      ))}
                    </span>
                  )}
                </Link>
              ))}
            </div>
          )}
        </section>
      </div>
    </MainLayout>
  );
}
