import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import MainLayout from "../../../../layouts/MainLayout.jsx";
import Avatar from "../../../../components/Avatar.jsx";
import Button from "../../../../components/Button.jsx";
import Tab from "../../../../components/Tab.jsx";
import BriefingTab from "../Briefing/BriefingTab.jsx";
import QnATab from "../QnA/QnATab.jsx";
import MembersTab from "../Members/MembersTab.jsx";
import IntegrationTab from "../Integration/IntegrationTab.jsx";
import { getProject, getProjectMembers, updateProjectStatus } from "../../../../api/project.js";
import { getTodayBriefing } from "../../../../api/briefing.js";
import { getRecentActivities } from "../../../../api/activity.js";
import { memberDisplayName } from "../../../../utils/member.js";
import { projectStatusLabel } from "../../../../utils/project.js";

// 명세에 description 필드가 없어 안전하게 가드 처리했습니다.
const STATUS_SELECT_CLASSES = {
  진행중: "bg-active/10 text-active",
  진행전: "bg-secondary text-primary",
  완료: "bg-success/10 text-success",
};
const STATUS_OPTIONS = ["PENDING", "IN_PROGRESS", "COMPLETED"];

function ProjectOverview({ members, briefing, activities }) {
  return (
    <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_220px]">
      <div className="rounded-xl border border-border bg-secondary/40 p-6">
        <div className="flex items-center gap-3">
          <Avatar name="MORROW" size="lg" />
          <div>
            <p className="font-semibold text-primary">MORROW</p>
            <p className="text-xs text-muted">프로젝트의 맥락을 기억하고 있어요</p>
          </div>
        </div>
        <h2 className="mt-6 text-xl font-bold text-primary">
          이 프로젝트에서 방금 있었던 일
        </h2>
        {briefing ? (
          <p className="mt-3 text-sm leading-6 text-muted">{briefing.summary}</p>
        ) : (
          <p className="mt-3 text-sm leading-6 text-muted">아직 오늘의 브리핑이 없어요.</p>
        )}
      </div>

      <aside className="rounded-xl border border-border bg-white p-5">
        <h2 className="font-semibold text-primary">팀원</h2>
        <div className="mt-4 space-y-3">
          {members.map((member) => (
            <div key={member.memberId} className="flex items-center gap-2">
              <Avatar name={memberDisplayName(member)} size="sm" />
              <div>
                <p className="text-sm font-medium text-primary">{memberDisplayName(member)}</p>
                <p className="text-xs text-muted">{member.role ?? "프로젝트 팀원"}</p>
              </div>
            </div>
          ))}
          {members.length === 0 && <p className="text-xs text-muted">팀원이 없어요.</p>}
        </div>
      </aside>

      <section className="lg:col-span-2">
        <h2 className="mb-3 text-base font-semibold text-primary">최근 활동</h2>
        {activities.length > 0 ? (
          <ul className="max-h-80 divide-y divide-border overflow-y-auto rounded-xl border border-border bg-white">
            {activities.map((activity, index) => (
              <li key={index} className="px-5 py-4 text-sm text-primary">{activity.description}</li>
            ))}
          </ul>
        ) : (
          <p className="rounded-xl border border-dashed border-border px-5 py-8 text-center text-sm text-muted">
            아직 최근 활동이 없어요.
          </p>
        )}
      </section>
    </div>
  );
}

const TAB_INDEX = { HOME: 0, BRIEFING: 1, QNA: 2, MEMBERS: 3, INTEGRATION: 4 };

export default function ProjectHomePage() {
  const { projectId } = useParams();
  const [activeTabIndex, setActiveTabIndex] = useState(TAB_INDEX.HOME);

  const [project, setProject] = useState(null);
  const [members, setMembers] = useState([]);
  const [briefing, setBriefing] = useState(null);
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [statusSaving, setStatusSaving] = useState(false);
  const [statusError, setStatusError] = useState(null);

  useEffect(() => {
    let ignore = false;

    Promise.all([
      getProject(projectId),
      getProjectMembers(projectId),
      // /briefings/today는 목록(ApiResponse<List<BriefingResponse>>)을 반환한다.
      // 오늘자는 최대 1건이라 첫 번째 항목만 꺼내 쓴다.
      getTodayBriefing(projectId).then(({ data }) => data.data?.[0] ?? null).catch(() => null),
      getRecentActivities(projectId).catch(() => ({ data: [] })),
    ])
      .then(([projectRes, membersRes, briefing, activitiesRes]) => {
        if (ignore) return;
        setProject(projectRes.data);
        setMembers(membersRes.data);
        setBriefing(briefing);
        setActivities(activitiesRes.data);
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
  }, [projectId]);

  const handleStatusChange = async (event) => {
    const nextStatus = event.target.value;
    setStatusSaving(true);
    setStatusError(null);
    try {
      const { data } = await updateProjectStatus(projectId, nextStatus);
      setProject(data);
    } catch {
      setStatusError("프로젝트 상태를 변경하지 못했어요.");
    } finally {
      setStatusSaving(false);
    }
  };

  if (loading) {
    return (
      <MainLayout>
        <p className="text-sm text-muted">불러오는 중...</p>
      </MainLayout>
    );
  }

  if (error || !project) {
    return (
      <MainLayout>
        <p className="text-sm text-danger">프로젝트를 불러오지 못했어요.</p>
      </MainLayout>
    );
  }

  return (
    <MainLayout>
      <div className="mx-auto max-w-5xl">
        <header className="mb-7 border-b border-border pb-6">
          <p className="mb-2 text-xs font-semibold tracking-[0.16em] text-muted">PROJECT</p>
          <div className="flex flex-wrap items-center gap-3">
            <h1 className="text-2xl font-bold text-primary">{project.name}</h1>
            {project.status && (
              <select
                value={project.status}
                onChange={handleStatusChange}
                disabled={statusSaving}
                className={`rounded-full border-0 px-2.5 py-0.5 text-xs font-medium ${STATUS_SELECT_CLASSES[projectStatusLabel(project.status)]}`}
              >
                {STATUS_OPTIONS.map((status) => (
                  <option key={status} value={status}>
                    {projectStatusLabel(status)}
                  </option>
                ))}
              </select>
            )}
            <Link to={`/projects/${project.id}/edit`}>
              <Button variant="secondary" className="px-3 py-1.5 text-xs">
                팀원 · 연동 관리
              </Button>
            </Link>
          </div>
          {project.description && (
            <p className="mt-3 text-sm text-muted">{project.description}</p>
          )}
          {statusError && <p className="mt-2 text-sm text-danger">{statusError}</p>}
        </header>

        <Tab
          activeIndex={activeTabIndex}
          onTabChange={setActiveTabIndex}
          tabs={[
            {
              label: "홈",
              content: <ProjectOverview members={members} briefing={briefing} activities={activities} />,
            },
            {
              label: "AI 브리핑 상세",
              content: (
                <BriefingTab
                  projectId={projectId}
                  onAskQuestion={() => setActiveTabIndex(TAB_INDEX.QNA)}
                />
              ),
            },
            { label: "컨텍스트 Q&A", content: <QnATab projectId={projectId} /> },
            { label: "팀원 관리", content: <MembersTab projectId={projectId} /> },
            { label: "연동 상태", content: <IntegrationTab projectId={projectId} /> },
          ]}
        />
      </div>
    </MainLayout>
  );
}
