import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import MainLayout from "../../../layouts/MainLayout.jsx";
import Avatar from "../../../components/Avatar.jsx";
import Badge from "../../../components/Badge.jsx";
import Button from "../../../components/Button.jsx";
import Input from "../../../components/Input.jsx";
import { getProjects } from "../../../api/project.js";

// 명세의 Project에는 status/description/period/members/tasks가 없습니다.
// 값이 있으면 표시하고, 없으면 자연스럽게 생략되도록 안전하게 처리했습니다.
const statusVariant = { 진행중: "active", 검토중: "danger", 완료: "success" };
const filters = ["전체", "진행중", "검토중", "완료"];

function formatDate(dateString) {
  if (!dateString) return "";
  return new Date(dateString).toLocaleDateString("ko-KR");
}

export default function ProjectListPage() {
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [keyword, setKeyword] = useState("");
  const [selectedFilter, setSelectedFilter] = useState("전체");
  const [view, setView] = useState("list");

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

  const filteredProjects = useMemo(
    () =>
      projects.filter((project) => {
        const matchesKeyword = [project.name, project.description]
          .filter(Boolean)
          .join(" ")
          .toLowerCase()
          .includes(keyword.toLowerCase());
        const matchesStatus =
          selectedFilter === "전체" || project.status === selectedFilter;
        return matchesKeyword && matchesStatus;
      }),
    [projects, keyword, selectedFilter],
  );

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
                    ? "border-primary bg-primary text-accent"
                    : "border-border bg-white text-muted hover:border-primary hover:text-primary"
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
                  view === "list" ? "bg-primary text-accent" : "text-muted"
                }`}
              >
                리스트
              </button>
              <button
                type="button"
                onClick={() => setView("card")}
                className={`rounded px-3 py-1.5 text-xs font-medium ${
                  view === "card" ? "bg-primary text-accent" : "text-muted"
                }`}
              >
                카드
              </button>
            </div>
          </div>
        </div>

        {loading && <p className="text-sm text-muted">불러오는 중...</p>}
        {error && <p className="text-sm text-danger">프로젝트를 불러오지 못했어요.</p>}

        {!loading && !error && (
          <>
            {view === "list" ? (
              <section className="overflow-hidden rounded-xl border border-border bg-white">
                <div className="hidden grid-cols-[minmax(0,1fr)_130px_170px_130px] gap-4 border-b border-border bg-secondary/60 px-5 py-3 text-xs font-medium text-muted md:grid">
                  <span>프로젝트</span><span>상태</span><span>생성일</span><span>멤버</span>
                </div>
                <div className="divide-y divide-border">
                  {filteredProjects.map((project) => (
                    <Link
                      key={project.id}
                      to={`/projects/${project.id}`}
                      className="grid gap-3 px-5 py-5 transition-colors hover:bg-secondary/50 md:grid-cols-[minmax(0,1fr)_130px_170px_130px] md:items-center md:gap-4"
                    >
                      <div className="min-w-0">
                        <h2 className="truncate font-semibold text-primary">{project.name}</h2>
                        {project.description && (
                          <p className="mt-1 truncate text-sm text-muted">{project.description}</p>
                        )}
                        <p className="mt-2 text-xs text-muted md:hidden">생성일 · {formatDate(project.createdAt)}</p>
                      </div>
                      <div>
                        {project.status && (
                          <Badge variant={statusVariant[project.status]}>{project.status}</Badge>
                        )}
                      </div>
                      <p className="hidden text-sm text-muted md:block">{formatDate(project.createdAt)}</p>
                      <div className="flex -space-x-2">
                        {project.members?.slice(0, 3).map((member) => (
                          <span key={member} className="rounded-full bg-white ring-2 ring-white">
                            <Avatar name={member} size="sm" />
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
                  <Link
                    key={project.id}
                    to={`/projects/${project.id}`}
                    className="rounded-xl border border-border bg-white p-5 transition-transform hover:-translate-y-0.5 hover:shadow-md"
                  >
                    <div className="flex items-start justify-between gap-2">
                      {project.status && (
                        <Badge variant={statusVariant[project.status]}>{project.status}</Badge>
                      )}
                      <span className="text-xs text-muted">{formatDate(project.createdAt)}</span>
                    </div>
                    <h2 className="mt-5 font-semibold text-primary">{project.name}</h2>
                    {project.description && (
                      <p className="mt-2 min-h-10 text-sm text-muted">{project.description}</p>
                    )}
                    <div className="mt-4 flex items-center justify-between border-t border-border pt-4">
                      <div className="flex -space-x-2">
                        {project.members?.slice(0, 3).map((member) => (
                          <span key={member} className="rounded-full bg-white ring-2 ring-white">
                            <Avatar name={member} size="sm" />
                          </span>
                        ))}
                      </div>
                      {project.tasks != null && (
                        <span className="text-xs text-muted">작업 {project.tasks}개</span>
                      )}
                    </div>
                  </Link>
                ))}
              </section>
            )}

            {filteredProjects.length === 0 && (
              <p className="rounded-xl border border-dashed border-border px-5 py-16 text-center text-sm text-muted">조건에 맞는 프로젝트가 없습니다.</p>
            )}
          </>
        )}
      </div>
    </MainLayout>
  );
}
