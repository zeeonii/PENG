import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import MainLayout from "../../../layouts/MainLayout.jsx";
import Avatar from "../../../components/Avatar.jsx";
import Badge from "../../../components/Badge.jsx";
import Button from "../../../components/Button.jsx";
import Input from "../../../components/Input.jsx";
import { useUser } from "../../../contexts/UserContext.jsx";
import {
  getProjects,
  getProjectMembers,
  updateProjectName,
  deleteProject,
} from "../../../api/project.js";
import { memberDisplayName } from "../../../utils/member.js";
import { projectStatusLabel } from "../../../utils/project.js";

const filters = ["전체", "진행전", "진행중", "완료"];

// 명세에 description/기간 필드가 없어 값이 있을 때만 표시합니다.
const statusVariant = { 진행중: "active", 진행전: "default", 완료: "success" };

// 프로젝트 카드/행 우측의 "⋯" 메뉴. 클릭 시 이벤트가 부모 Link로 번지지
// 않도록 각 핸들러에서 stopPropagation 합니다.
function ProjectActionsMenu({ isOwner, isOpen, onToggle, onEdit, onDelete, deleting }) {
  return (
    <div className="relative shrink-0" onClick={(event) => event.stopPropagation()}>
      <button
        type="button"
        onClick={(event) => {
          event.preventDefault();
          onToggle();
        }}
        aria-label="프로젝트 메뉴"
        className="rounded-full p-1.5 text-muted hover:bg-secondary hover:text-primary"
      >
        ⋯
      </button>
      {isOpen && (
        <>
          <div
            className="fixed inset-0 z-10"
            onClick={(event) => {
              event.preventDefault();
              onToggle();
            }}
          />
          <div className="absolute right-0 z-20 mt-1 w-32 overflow-hidden rounded-lg border border-border bg-white py-1 shadow-lg">
            <button
              type="button"
              onClick={(event) => {
                event.preventDefault();
                onEdit();
              }}
              className="block w-full px-3 py-2 text-left text-sm text-primary hover:bg-secondary"
            >
              수정
            </button>
            {isOwner && (
              <button
                type="button"
                onClick={(event) => {
                  event.preventDefault();
                  onDelete();
                }}
                disabled={deleting}
                className="block w-full px-3 py-2 text-left text-sm text-danger hover:bg-secondary"
              >
                {deleting ? "삭제 중..." : "삭제"}
              </button>
            )}
          </div>
        </>
      )}
    </div>
  );
}

export default function ProjectListPage() {
  const { user } = useUser();
  const [keyword, setKeyword] = useState("");
  const [selectedFilter, setSelectedFilter] = useState("전체");
  const [view, setView] = useState("list");

  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [openMenuId, setOpenMenuId] = useState(null);
  const [editingId, setEditingId] = useState(null);
  const [editingName, setEditingName] = useState("");
  const [renameSaving, setRenameSaving] = useState(false);
  const [deletingId, setDeletingId] = useState(null);
  const [actionError, setActionError] = useState(null);

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

  const startEditing = (project) => {
    setActionError(null);
    setOpenMenuId(null);
    setEditingId(project.id);
    setEditingName(project.name);
  };

  const cancelEditing = () => {
    setEditingId(null);
    setEditingName("");
  };

  const saveEditing = async () => {
    const trimmed = editingName.trim();
    if (!trimmed) return;

    setRenameSaving(true);
    setActionError(null);
    try {
      const { data } = await updateProjectName(editingId, trimmed);
      setProjects((prev) => prev.map((p) => (p.id === editingId ? { ...p, name: data.name } : p)));
      cancelEditing();
    } catch {
      setActionError("프로젝트 이름을 수정하지 못했어요.");
    } finally {
      setRenameSaving(false);
    }
  };

  const handleDelete = async (project) => {
    setOpenMenuId(null);
    if (!window.confirm(`"${project.name}" 프로젝트를 삭제할까요? 되돌릴 수 없어요.`)) return;

    setDeletingId(project.id);
    setActionError(null);
    try {
      await deleteProject(project.id);
      setProjects((prev) => prev.filter((p) => p.id !== project.id));
    } catch {
      setActionError("프로젝트를 삭제하지 못했어요.");
    } finally {
      setDeletingId(null);
    }
  };

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

        {actionError && <p className="mb-4 text-sm text-danger">{actionError}</p>}

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
            <div className="hidden grid-cols-[minmax(0,1fr)_130px_130px_40px] gap-4 border-b border-border bg-secondary/60 px-5 py-3 text-xs font-medium text-muted md:grid">
              <span>프로젝트</span><span>상태</span><span>멤버</span><span />
            </div>
            <div className="divide-y divide-border">
              {filteredProjects.map((project) => (
                <Link
                  key={project.id}
                  to={editingId === project.id ? "#" : `/projects/${project.id}`}
                  onClick={(event) => {
                    if (editingId === project.id) event.preventDefault();
                  }}
                  className="grid gap-3 px-5 py-5 transition-colors hover:bg-secondary/50 md:grid-cols-[minmax(0,1fr)_130px_130px_40px] md:items-center md:gap-4"
                >
                  <div className="min-w-0" onClick={(event) => editingId === project.id && event.stopPropagation()}>
                    {editingId === project.id ? (
                      <div className="flex items-center gap-2">
                        <Input
                          value={editingName}
                          onChange={(event) => setEditingName(event.target.value)}
                          onKeyDown={(event) => {
                            if (event.key === "Enter") saveEditing();
                            if (event.key === "Escape") cancelEditing();
                          }}
                          disabled={renameSaving}
                          autoFocus
                          className="text-sm"
                        />
                        <button type="button" onClick={saveEditing} disabled={renameSaving} className="text-xs font-semibold text-active">
                          저장
                        </button>
                        <button type="button" onClick={cancelEditing} className="text-xs text-muted">
                          취소
                        </button>
                      </div>
                    ) : (
                      <>
                        <h2 className="truncate font-semibold text-primary">{project.name}</h2>
                        {project.description && (
                          <p className="mt-1 truncate text-sm text-muted">{project.description}</p>
                        )}
                      </>
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
                  <ProjectActionsMenu
                    isOwner={project.createdBy === user?.id}
                    isOpen={openMenuId === project.id}
                    onToggle={() => setOpenMenuId((current) => (current === project.id ? null : project.id))}
                    onEdit={() => startEditing(project)}
                    onDelete={() => handleDelete(project)}
                    deleting={deletingId === project.id}
                  />
                </Link>
              ))}
            </div>
          </section>
        ) : (
          <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
            {filteredProjects.map((project) => (
              <Link
                key={project.id}
                to={editingId === project.id ? "#" : `/projects/${project.id}`}
                onClick={(event) => {
                  if (editingId === project.id) event.preventDefault();
                }}
                className="rounded-xl border border-border bg-white p-5 transition-transform hover:-translate-y-0.5 hover:shadow-md"
              >
                <div className="flex items-start justify-between gap-2">
                  {project.status && (
                    <Badge variant={statusVariant[projectStatusLabel(project.status)]}>
                      {projectStatusLabel(project.status)}
                    </Badge>
                  )}
                  <ProjectActionsMenu
                    isOwner={project.createdBy === user?.id}
                    isOpen={openMenuId === project.id}
                    onToggle={() => setOpenMenuId((current) => (current === project.id ? null : project.id))}
                    onEdit={() => startEditing(project)}
                    onDelete={() => handleDelete(project)}
                    deleting={deletingId === project.id}
                  />
                </div>
                <div className="mt-5" onClick={(event) => editingId === project.id && event.stopPropagation()}>
                  {editingId === project.id ? (
                    <div className="flex items-center gap-2">
                      <Input
                        value={editingName}
                        onChange={(event) => setEditingName(event.target.value)}
                        onKeyDown={(event) => {
                          if (event.key === "Enter") saveEditing();
                          if (event.key === "Escape") cancelEditing();
                        }}
                        disabled={renameSaving}
                        autoFocus
                        className="text-sm"
                      />
                      <button type="button" onClick={saveEditing} disabled={renameSaving} className="text-xs font-semibold text-active">
                        저장
                      </button>
                      <button type="button" onClick={cancelEditing} className="text-xs text-muted">
                        취소
                      </button>
                    </div>
                  ) : (
                    <>
                      <h2 className="font-semibold text-primary">{project.name}</h2>
                      {project.description && (
                        <p className="mt-2 min-h-10 text-sm text-muted">{project.description}</p>
                      )}
                    </>
                  )}
                </div>
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
