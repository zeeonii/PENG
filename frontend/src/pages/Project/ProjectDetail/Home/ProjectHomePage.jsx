import { Link, useParams } from "react-router-dom";
import MainLayout from "../../../../layouts/MainLayout.jsx";
import Avatar from "../../../../components/Avatar.jsx";
import Badge from "../../../../components/Badge.jsx";
import Button from "../../../../components/Button.jsx";
import Tab from "../../../../components/Tab.jsx";
import { projects, statusVariant } from "../../../../mock/projectData.js";

function ProjectOverview({ project }) {
  return (
    <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_220px]">
      <div className="rounded-xl border border-border bg-secondary/40 p-6">
        <div className="flex items-center gap-3">
          <Avatar name="레미" size="lg" />
          <div>
            <p className="font-semibold text-primary">레미</p>
            <p className="text-xs text-muted">프로젝트의 맥락을 기억하고 있어요</p>
          </div>
        </div>
        <h2 className="mt-6 text-xl font-bold text-primary">
          이 프로젝트에서 방금 있었던 일
        </h2>
        <p className="mt-3 text-sm leading-6 text-muted">
          API 설계 회의 후 인증 방식이 JWT로 결정되었고, 온보딩 가이드 문서가 수정되었습니다.
        </p>
        <button type="button" className="mt-5 text-sm font-medium text-primary underline underline-offset-4">
          근거 2건 보기
        </button>
      </div>

      <aside className="rounded-xl border border-border bg-white p-5">
        <h2 className="font-semibold text-primary">팀원</h2>
        <div className="mt-4 space-y-3">
          {project.members.map((member, index) => (
            <div key={member} className="flex items-center gap-2">
              <Avatar name={member} size="sm" />
              <div>
                <p className="text-sm font-medium text-primary">{member}</p>
                <p className="text-xs text-muted">{index === 0 ? "프론트엔드" : "프로젝트 팀원"}</p>
              </div>
            </div>
          ))}
        </div>
      </aside>

      <section className="lg:col-span-2">
        <h2 className="mb-3 text-base font-semibold text-primary">최근 활동</h2>
        <ul className="overflow-hidden rounded-xl border border-border bg-white divide-y divide-border">
          <li className="px-5 py-4 text-sm text-primary">회의록이 업데이트되었습니다.</li>
          <li className="px-5 py-4 text-sm text-primary">Notion 문서에 온보딩 가이드가 추가되었습니다.</li>
          <li className="px-5 py-4 text-sm text-primary">인증 방식 관련 결정사항이 기록되었습니다.</li>
        </ul>
      </section>
    </div>
  );
}

function EmptyTab({ label }) {
  return (
    <div className="rounded-xl border border-dashed border-border px-5 py-10 text-center text-sm text-muted">
      {label} 화면은 담당 기능과 연결될 예정입니다.
    </div>
  );
}

export default function ProjectHomePage() {
  const { projectId } = useParams();
  const project = projects.find((item) => item.id === projectId) ?? projects[0];

  return (
    <MainLayout>
      <div className="mx-auto max-w-5xl">
        <header className="mb-7 border-b border-border pb-6">
          <p className="mb-2 text-xs font-semibold tracking-[0.16em] text-muted">PROJECT</p>
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-2xl font-bold text-primary">{project.title}</h1>
            <Badge variant={statusVariant[project.status]}>{project.status}</Badge>
            <Link to={`/projects/${project.id}/edit`}>
              <Button variant="secondary" className="px-3 py-1.5 text-xs">
                수정
              </Button>
            </Link>
          </div>
          <p className="mt-3 text-sm text-muted">{project.description}</p>
          <p className="mt-2 text-sm text-muted">프로젝트 기간 · {project.period}</p>
        </header>

        <Tab
          tabs={[
            { label: "홈", content: <ProjectOverview project={project} /> },
            { label: "AI 브리핑 상세", content: <EmptyTab label="AI 브리핑 상세" /> },
            { label: "컨텍스트 Q&A", content: <EmptyTab label="컨텍스트 Q&A" /> },
            { label: "팀원 관리", content: <EmptyTab label="팀원 관리" /> },
            { label: "연동 상태", content: <EmptyTab label="연동 상태" /> },
          ]}
        />
      </div>
    </MainLayout>
  );
}
