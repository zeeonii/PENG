import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import MainLayout from "../../../layouts/MainLayout.jsx";
import Button from "../../../components/Button.jsx";
import Input from "../../../components/Input.jsx";
import Avatar from "../../../components/Avatar.jsx";
import {
  createProject,
  getProject,
  getProjectMembers,
  inviteProjectMember,
  removeProjectMember,
} from "../../../api/project.js";
import { connectNotion, connectGoogleMeet } from "../../../api/integration.js";
import GoogleMeetIcon from "../../../components/icons/GoogleMeetIcon.jsx";
import NotionIcon from "../../../components/icons/NotionIcon.jsx";
import { memberDisplayName } from "../../../utils/member.js";

// 명세상 프로젝트 이름 수정 API(PATCH /projects/{id})가 없어 이 화면은
// "수정" 대신 기존 프로젝트의 팀원·연동을 관리하는 용도로 동작합니다.
const emptyInvite = { email: "", role: "" };

const INTEGRATIONS = [
  {
    type: "NOTION",
    label: "Notion",
    description: "프로젝트 관련 문서를 함께 동기화합니다.",
    Icon: NotionIcon,
    connect: connectNotion,
  },
  {
    type: "GOOGLE_MEET",
    label: "Google Meet",
    description: "회의록을 자동으로 수집해 동기화합니다.",
    Icon: GoogleMeetIcon,
    connect: connectGoogleMeet,
  },
];

export default function ProjectCreatePage() {
  const navigate = useNavigate();
  const { projectId } = useParams();
  const isEditing = Boolean(projectId);

  const [projectName, setProjectName] = useState("");
  const [loadError, setLoadError] = useState(null);
  const [members, setMembers] = useState([]);
  const [invites, setInvites] = useState([emptyInvite]);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState(null);
  const [connecting, setConnecting] = useState(null);

  useEffect(() => {
    if (!isEditing) return;
    let ignore = false;

    Promise.all([getProject(projectId), getProjectMembers(projectId)])
      .then(([projectRes, membersRes]) => {
        if (ignore) return;
        setProjectName(projectRes.data.name);
        setMembers(membersRes.data);
      })
      .catch((err) => {
        if (!ignore) setLoadError(err);
      });

    return () => {
      ignore = true;
    };
  }, [isEditing, projectId]);

  const updateInvite = (index, key, value) => {
    setInvites((current) =>
      current.map((invite, i) => (i === index ? { ...invite, [key]: value } : invite)),
    );
  };

  const removeInviteRow = (index) => {
    if (invites.length === 1) return;
    setInvites((current) => current.filter((_, i) => i !== index));
  };

  const removeMember = async (memberId) => {
    try {
      await removeProjectMember(projectId, memberId);
      setMembers((current) => current.filter((member) => member.memberId !== memberId));
    } catch {
      setSubmitError("팀원을 내보내지 못했어요.");
    }
  };

  const handleConnect = async (integration) => {
    setConnecting(integration.type);
    try {
      await integration.connect(projectId);
    } catch {
      setSubmitError(`${integration.label} 연동을 시작하지 못했어요.`);
      setConnecting(null);
    }
  };

  const inviteMembersTo = async (targetProjectId) => {
    const validInvites = invites.filter((invite) => invite.email.trim());
    const results = await Promise.allSettled(
      validInvites.map((invite) =>
        inviteProjectMember(targetProjectId, {
          email: invite.email.trim(),
          role: invite.role.trim() || undefined,
        }),
      ),
    );
    const failed = results.filter((result) => result.status === "rejected");
    return failed.length;
  };

  const handleCreate = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setSubmitError(null);

    try {
      const { data: newProject } = await createProject({ name: projectName });
      const failedCount = await inviteMembersTo(newProject.id);

      if (failedCount > 0) {
        setSubmitError(
          `프로젝트는 생성되었지만 팀원 ${failedCount}명 초대에 실패했어요. 상세 화면에서 다시 시도해주세요.`,
        );
        setTimeout(() => navigate(`/projects/${newProject.id}`), 1500);
        return;
      }

      navigate(`/projects/${newProject.id}`);
    } catch {
      setSubmitError("프로젝트를 생성하지 못했어요.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleInviteExisting = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setSubmitError(null);

    const failedCount = await inviteMembersTo(projectId);
    if (failedCount > 0) {
      setSubmitError(`팀원 ${failedCount}명 초대에 실패했어요. 이미 가입한 이메일인지 확인해주세요.`);
    } else {
      const { data } = await getProjectMembers(projectId);
      setMembers(data);
      setInvites([emptyInvite]);
    }
    setSubmitting(false);
  };

  if (isEditing && loadError) {
    return (
      <MainLayout>
        <p className="text-sm text-danger">프로젝트 정보를 불러오지 못했어요.</p>
      </MainLayout>
    );
  }

  return (
    <MainLayout>
      <div className="mx-auto max-w-3xl">
        <header className="mb-8">
          <p className="mb-2 text-xs font-semibold tracking-[0.16em] text-muted">
            PROJECT
          </p>
          <h1 className="text-2xl font-bold text-primary">
            {isEditing ? "팀원 · 연동 관리" : "새 프로젝트 만들기"}
          </h1>
          <p className="mt-2 text-sm text-muted">
            {isEditing
              ? "팀원을 초대하거나 내보내고, 외부 서비스 연동을 관리할 수 있습니다."
              : "프로젝트를 만들고 팀원과 함께 협업을 시작하세요."}
          </p>
        </header>

        {submitError && (
          <p className="mb-4 rounded-lg bg-danger/10 px-4 py-3 text-sm text-danger">
            {submitError}
          </p>
        )}

        {!isEditing ? (
          <form onSubmit={handleCreate} className="space-y-7 rounded-xl border border-border bg-white p-6 sm:p-8">
            <section>
              <h2 className="mb-4 text-base font-semibold text-primary">프로젝트 정보</h2>
              <label className="block text-sm font-medium text-primary">
                프로젝트명
                <span className="ml-1 text-danger">*</span>
                <Input
                  placeholder="예: 글로벌 협업 프로젝트"
                  value={projectName}
                  onChange={(event) => setProjectName(event.target.value)}
                  required
                  className="mt-2"
                />
              </label>
            </section>

            <InviteRows invites={invites} onUpdate={updateInvite} onRemove={removeInviteRow}
              onAdd={() => setInvites((current) => [...current, emptyInvite])} />

            <p className="border-t border-border pt-7 text-xs text-muted">
              Notion·Google Meet 연동은 프로젝트 생성 후 관리 화면에서 할 수 있어요.
            </p>

            <div className="flex justify-end gap-2 border-t border-border pt-6">
              <Button variant="secondary" onClick={() => navigate(-1)} disabled={submitting}>
                취소
              </Button>
              <Button type="submit" disabled={submitting}>
                {submitting ? "생성 중..." : "프로젝트 생성"}
              </Button>
            </div>
          </form>
        ) : (
          <div className="space-y-7">
            <section className="rounded-xl border border-border bg-white p-6 sm:p-8">
              <h2 className="mb-4 text-base font-semibold text-primary">프로젝트명</h2>
              <p className="text-sm text-primary">{projectName || "불러오는 중..."}</p>
            </section>

            <section className="rounded-xl border border-border bg-white p-6 sm:p-8">
              <h2 className="mb-4 text-base font-semibold text-primary">현재 팀원</h2>
              <div className="space-y-2">
                {members.map((member) => (
                  <div key={member.memberId} className="flex items-center justify-between gap-3 rounded-lg border border-border px-4 py-3">
                    <span className="flex items-center gap-2">
                      <Avatar name={memberDisplayName(member)} size="sm" />
                      <span className="text-sm text-primary">{memberDisplayName(member)}</span>
                      {member.role && <span className="text-xs text-muted">· {member.role}</span>}
                    </span>
                    <Button variant="secondary" className="px-3 py-1.5 text-xs" onClick={() => removeMember(member.memberId)}>
                      내보내기
                    </Button>
                  </div>
                ))}
                {members.length === 0 && (
                  <p className="text-sm text-muted">팀원이 없어요.</p>
                )}
              </div>
            </section>

            <form onSubmit={handleInviteExisting} className="rounded-xl border border-border bg-white p-6 sm:p-8">
              <InviteRows invites={invites} onUpdate={updateInvite} onRemove={removeInviteRow}
                onAdd={() => setInvites((current) => [...current, emptyInvite])} />
              <div className="mt-4 flex justify-end">
                <Button type="submit" disabled={submitting}>
                  {submitting ? "초대 중..." : "팀원 초대하기"}
                </Button>
              </div>
            </form>

            <section className="rounded-xl border border-border bg-white p-6 sm:p-8">
              <h2 className="text-base font-semibold text-primary">연동 관리</h2>
              <div className="mt-4 space-y-3">
                {INTEGRATIONS.map((integration) => (
                  <div
                    key={integration.type}
                    className="flex items-center justify-between gap-4 rounded-lg border border-border p-4"
                  >
                    <div className="flex items-center gap-3">
                      <integration.Icon className="h-8 w-8 shrink-0" />
                      <div>
                        <p className="text-sm font-medium text-primary">{integration.label}</p>
                        <p className="mt-1 text-xs text-muted">{integration.description}</p>
                      </div>
                    </div>
                    <Button
                      variant="secondary"
                      onClick={() => handleConnect(integration)}
                      disabled={connecting === integration.type}
                    >
                      {connecting === integration.type ? "연결 중..." : "연동하기"}
                    </Button>
                  </div>
                ))}
              </div>
            </section>

            <div className="flex justify-end border-t border-border pt-6">
              <Button onClick={() => navigate(`/projects/${projectId}`)}>완료</Button>
            </div>
          </div>
        )}
      </div>
    </MainLayout>
  );
}

function InviteRows({ invites, onUpdate, onRemove, onAdd }) {
  return (
    <section>
      <div className="mb-4 flex items-center justify-between gap-3">
        <div>
          <h2 className="text-base font-semibold text-primary">팀원 초대</h2>
          <p className="mt-1 text-xs text-muted">이미 가입한 회원의 이메일만 초대할 수 있어요.</p>
        </div>
        <Button variant="secondary" className="shrink-0 px-3 py-1.5 text-xs" onClick={onAdd}>
          + 팀원 추가
        </Button>
      </div>

      <div className="space-y-2">
        {invites.map((invite, index) => (
          <div key={index} className="grid gap-2 sm:grid-cols-[minmax(0,1fr)_160px_auto]">
            <Input
              type="email"
              placeholder="이메일 주소"
              value={invite.email}
              onChange={(event) => onUpdate(index, "email", event.target.value)}
            />
            <Input
              placeholder="담당 업무 (선택)"
              value={invite.role}
              onChange={(event) => onUpdate(index, "role", event.target.value)}
            />
            <Button
              variant="secondary"
              className="px-3 py-2 text-xs"
              onClick={() => onRemove(index)}
              disabled={invites.length === 1}
            >
              삭제
            </Button>
          </div>
        ))}
      </div>
    </section>
  );
}
