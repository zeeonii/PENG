import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import MainLayout from "../../../layouts/MainLayout.jsx";
import Avatar from "../../../components/Avatar.jsx";
import Badge from "../../../components/Badge.jsx";
import Button from "../../../components/Button.jsx";
import Input from "../../../components/Input.jsx";
import { getProjects, getProjectMembers } from "../../../api/project.js";
import { memberDisplayName } from "../../../utils/member.js";
import { projectStatusLabel } from "../../../utils/project.js";

const filters = ["전체", "진행전", "진행중", "완료"];

// 명세에 description/기간 필드가 없어 값이 있을 때만 표시합니다.
const statusVariant = { 진행중: "active", 진행전: "default", 완료: "success" };

export default function ProjectListPage() {
  const [keyword, setKeyword] = useState("");
  const [selectedFilter, setSelectedFilter] = useState("전체");
  const [view, setView] = useState("list");

  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

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

  const filteredProjects = useMemo(
    () =>
      projects.filter((project) => {
        const matchesKeyword = project.name.toLowerCase().includes(keyword.toLowerCase());
        const matchesStatus =
          selectedFilter === "전체" || projectStatusLabel(project.status) === selectedFilter;
        return matchesKeyword && matchesStatus;
      }),
    [projects, keyword, selectedFilter],
  );

  if (loading) {
    return (
      <MainLayout>
        <p className="text-sm text-muted">불러오는 중...</p>
      </MainLayout>
    );
  }

  if (error) {
    return (
      <MainLayout>
        <p className="text-sm text-danger">프로젝트 목록을 불러오지 못했어요.</p>
      </MainLayout>
    );
  }

  return (
    <MainLayout>
      <div className="mx-auto max-w-5xl">
        <header className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="mb-2 text-xs font-semibold tracking-[0.16em] text-muted">PROJECTS</p>
            <h1 className="text-2xl font-bold text-primary">프로젝트</h1>
            <p className="mt-2 text-sm text-muted">팀의 프로젝트와 진행 상황을 한눈에 확인하세요.</p>
          </div>
          <Link to="/projects/new"><Button>새 프로젝트 만들기</Button></Link>
        </header>

        <div className="mb-5 flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
          <div className="flex flex-wrap gap-2">
            {filters.map((filter) => (
              <button
                key={filter}
                type="button"
                onClick={() => setSelectedFilter(filter)}
                className={`rounded-full border px-3 py-1.5 text-sm transition-colors ${
                  selectedFilter === filter
                    ? "border-active bg-active text-white"
                    : "border-border bg-white text-muted hover:border-active hover:text-primary"
                }`}
              >
                {filter}
              </button>
            ))}
          </div>

          <div className="flex gap-2">
            <Input
              placeholder="프로젝트 검색"
              value={keyword}
              onChange={(event) => setKeyword(event.target.value)}
              className="w-full lg:w-52"
            />
            <div className="flex shrink-0 rounded-md border border-border bg-white p-1">
              <button
                type="button"
                onClick={() => setView("list")}
                className={`rounded px-3 py-1.5 text-xs font-medium ${
                  view === "list" ? "bg-active text-white" : "text-muted"
                }`}
              >
                리스트
              </button>
              <button
                type="button"
                onClick={() => setView("card")}
                className={`rounded px-3 py-1.5 text-xs font-medium ${
                  view === "card" ? "bg-active text-white" : "text-muted"
                }`}
              >
                카드
              </button>
            </div>
          </div>
        </div>

        {view === "list" ? (
          <section className="overflow-hidden rounded-xl border border-border bg-white">
            <div className="hidden grid-cols-[minmax(0,1fr)_130px_130px] gap-4 border-b border-border bg-secondary/60 px-5 py-3 text-xs font-medium text-muted md:grid">
              <span>프로젝트</span><span>상태</span><span>멤버</span>
            </div>
            <div className="divide-y divide-border">
              {filteredProjects.map((project) => (
                <Link
                  key={project.id}
                  to={`/projects/${project.id}`}
                  className="grid gap-3 px-5 py-5 transition-colors hover:bg-secondary/50 md:grid-cols-[minmax(0,1fr)_130px_130px] md:items-center md:gap-4"
                >
                  <div className="min-w-0">
                    <h2 className="truncate font-semibold text-primary">{project.name}</h2>
                    {project.description && (
                      <p className="mt-1 truncate text-sm text-muted">{project.description}</p>
                    )}
                  </div>
                  <div>
                    {project.status && (
                      <Badge variant={statusVariant[projectStatusLabel(project.status)]}>
                        {projectStatusLabel(project.status)}
                      </Badge>
                    )}
                  </div>
                  <div className="flex -space-x-1">
                    {project.members.slice(0, 3).map((member) => (
                      <span key={member.memberId} className="rounded-full bg-white ring-2 ring-white">
                        <Avatar name={memberDisplayName(member)} size="sm" />
                      </span>
                    ))}
                  </div>
                </Link>
              ))}
            </div>
          </section>
        ) : (
          <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
            {filteredProjects.map((project) => (
              <Link key={project.id} to={`/projects/${project.id}`} className="rounded-xl border border-border bg-white p-5 transition-transform hover:-translate-y-0.5 hover:shadow-md">
                {project.status && (
                  <Badge variant={statusVariant[projectStatusLabel(project.status)]}>
                    {projectStatusLabel(project.status)}
                  </Badge>
                )}
                <h2 className="mt-5 font-semibold text-primary">{project.name}</h2>
                {project.description && (
                  <p className="mt-2 min-h-10 text-sm text-muted">{project.description}</p>
                )}
                <div className="mt-4 flex -space-x-1 border-t border-border pt-4">
                  {project.members.slice(0, 3).map((member) => (
                    <span key={member.memberId} className="rounded-full bg-white ring-2 ring-white">
                      <Avatar name={memberDisplayName(member)} size="sm" />
                    </span>
                  ))}
                </div>
              </Link>
            ))}
          </section>
        )}

        {filteredProjects.length === 0 && (
          <p className="rounded-xl border border-dashed border-border px-5 py-16 text-center text-sm text-muted">조건에 맞는 프로젝트가 없습니다.</p>
        )}
      </div>
    </MainLayout>
  );
}
