/**
 * 프로젝트 상세 > 팀원 관리 탭
 *
 * 사용 예시:
 * <MembersTab projectId={project.id} />
 *
 * 삭제 불가 여부(생성자)는 미리 판단하지 않고 백엔드가 거부했을 때
 * 에러 메시지로 안내합니다. AI 팀원은 role 수정이 불가하여 "⋯" 메뉴에
 * 역할 수정 항목을 넣지 않습니다.
 */

import { useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import Avatar from "../../../../components/Avatar.jsx";
import Button from "../../../../components/Button.jsx";
import Input from "../../../../components/Input.jsx";
import {
  getProjectMembers,
  inviteProjectMember,
  removeProjectMember,
  updateProjectMemberRole,
} from "../../../../api/project.js";
import { memberDisplayName } from "../../../../utils/member.js";

// 팀원 행 우측의 "⋯" 메뉴. 드롭다운은 document.body에 포털로 렌더링합니다.
// 표를 감싼 컨테이너가 overflow-x-auto라 안에 absolute로 띄우면 아래쪽
// 행 근처 메뉴가 잘려 보이는 문제(ProjectListPage에서 겪은 것과 동일)가 있어 회피합니다.
function MemberActionsMenu({ isOpen, onToggle, canEditRole, onEditRole, onDelete }) {
  const buttonRef = useRef(null);
  const [position, setPosition] = useState(null);

  const handleToggle = () => {
    if (!isOpen && buttonRef.current) {
      const rect = buttonRef.current.getBoundingClientRect();
      setPosition({ top: rect.bottom + 4, right: window.innerWidth - rect.right });
    }
    onToggle();
  };

  return (
    <div className="relative shrink-0">
      <button
        type="button"
        ref={buttonRef}
        onClick={handleToggle}
        aria-label="팀원 메뉴"
        className="rounded-full p-1.5 text-muted hover:bg-secondary hover:text-primary"
      >
        ⋯
      </button>
      {isOpen &&
        position &&
        createPortal(
          <>
            <div className="fixed inset-0 z-40" onClick={onToggle} />
            <div
              style={{ position: "fixed", top: position.top, right: position.right }}
              className="z-50 w-32 overflow-hidden rounded-lg border border-border bg-white py-1 shadow-lg"
            >
              {canEditRole && (
                <button
                  type="button"
                  onClick={onEditRole}
                  className="block w-full px-3 py-2 text-left text-sm text-primary hover:bg-secondary"
                >
                  역할 수정
                </button>
              )}
              <button
                type="button"
                onClick={onDelete}
                className="block w-full px-3 py-2 text-left text-sm text-danger hover:bg-secondary"
              >
                삭제
              </button>
            </div>
          </>,
          document.body,
        )}
    </div>
  );
}

export default function MembersTab({ projectId }) {
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [inviting, setInviting] = useState(false);
  const [inviteEmail, setInviteEmail] = useState("");
  const [inviteRole, setInviteRole] = useState("");
  const [inviteError, setInviteError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const [openMenuId, setOpenMenuId] = useState(null);

  const [editingRoleId, setEditingRoleId] = useState(null);
  const [editingRoleValue, setEditingRoleValue] = useState("");
  const [roleSaving, setRoleSaving] = useState(false);
  const [roleError, setRoleError] = useState(null);

  useEffect(() => {
    let ignore = false;

    getProjectMembers(projectId)
      .then(({ data }) => {
        if (!ignore) setMembers(data);
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

  const handleRemove = async (memberId, name) => {
    setOpenMenuId(null);
    try {
      await removeProjectMember(projectId, memberId);
      setMembers((prev) => prev.filter((member) => member.memberId !== memberId));
    } catch {
      setError(new Error(`${name}님을 내보낼 수 없어요. (프로젝트 생성자는 내보낼 수 없습니다)`));
    }
  };

  const startEditingRole = (member) => {
    setOpenMenuId(null);
    setRoleError(null);
    setEditingRoleId(member.memberId);
    setEditingRoleValue(member.role ?? "");
  };

  const cancelEditingRole = () => {
    setEditingRoleId(null);
    setEditingRoleValue("");
  };

  const saveEditingRole = async () => {
    const trimmed = editingRoleValue.trim();
    if (!trimmed) return;

    setRoleSaving(true);
    setRoleError(null);
    try {
      const { data } = await updateProjectMemberRole(projectId, editingRoleId, trimmed);
      setMembers((prev) =>
        prev.map((member) => (member.memberId === editingRoleId ? { ...member, role: data.role } : member)),
      );
      cancelEditingRole();
    } catch {
      setRoleError("역할을 수정하지 못했어요.");
    } finally {
      setRoleSaving(false);
    }
  };

  const handleInvite = async (event) => {
    event.preventDefault();
    const email = inviteEmail.trim();
    if (!email || submitting) return;

    setSubmitting(true);
    setInviteError(null);
    try {
      await inviteProjectMember(projectId, { email, role: inviteRole.trim() || undefined });
      const { data } = await getProjectMembers(projectId);
      setMembers(data);
      setInviteEmail("");
      setInviteRole("");
      setInviting(false);
    } catch {
      setInviteError("초대에 실패했어요. 이미 가입한 이메일인지 확인해주세요.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h3 className="text-lg font-bold text-primary">
          팀원 ({members.length})
        </h3>
        <Button variant="secondary" onClick={() => setInviting((current) => !current)}>
          ＋ 팀원 초대
        </Button>
      </div>

      {inviting && (
        <form onSubmit={handleInvite} className="mb-4 flex gap-2">
          <Input
            type="email"
            placeholder="초대할 팀원의 이메일"
            value={inviteEmail}
            onChange={(event) => setInviteEmail(event.target.value)}
            required
          />
          <Input
            placeholder="역할 (선택, 예: PM)"
            value={inviteRole}
            onChange={(event) => setInviteRole(event.target.value)}
            className="w-40"
          />
          <Button type="submit" disabled={submitting}>
            {submitting ? "초대 중..." : "초대"}
          </Button>
        </form>
      )}
      {inviteError && <p className="mb-4 text-sm text-danger">{inviteError}</p>}
      {roleError && <p className="mb-4 text-sm text-danger">{roleError}</p>}

      {loading && <p className="text-sm text-muted">불러오는 중...</p>}
      {error && <p className="mb-4 text-sm text-danger">{error.message}</p>}

      {!loading && (
        <div className="overflow-x-auto rounded-xl border border-border bg-white">
          <table className="w-full min-w-[560px] table-fixed text-left text-sm">
            <colgroup>
              <col className="w-[30%]" />
              <col className="w-[22%]" />
              <col className="w-[26%]" />
              <col className="w-[14%]" />
              <col className="w-[8%]" />
            </colgroup>
            <thead className="border-b border-border text-xs text-muted">
              <tr>
                <th className="px-4 py-3 font-medium">이름</th>
                <th className="px-4 py-3 font-medium">역할</th>
                <th className="px-4 py-3 font-medium">담당 업무</th>
                <th className="px-4 py-3 font-medium">국가 · 시간대</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {members.map((member) => (
                <tr key={member.memberId}>
                  <td className="px-4 py-3">
                    <span className="flex items-center gap-2 overflow-hidden">
                      <Avatar name={memberDisplayName(member)} size="sm" />
                      <strong className="truncate font-medium text-primary">
                        {memberDisplayName(member)}
                      </strong>
                    </span>
                  </td>
                  <td className="px-4 py-3 text-muted">
                    {editingRoleId === member.memberId ? (
                      <Input
                        value={editingRoleValue}
                        onChange={(event) => setEditingRoleValue(event.target.value)}
                        onKeyDown={(event) => {
                          if (event.key === "Enter") saveEditingRole();
                          if (event.key === "Escape") cancelEditingRole();
                        }}
                        disabled={roleSaving}
                        autoFocus
                        className="text-sm"
                      />
                    ) : (
                      <span className="block truncate">{member.role ?? "-"}</span>
                    )}
                  </td>
                  <td className="truncate px-4 py-3 text-muted">{member.duty ?? "-"}</td>
                  <td className="truncate px-4 py-3 text-muted">
                    {member.country
                      ? member.timezone
                        ? `${member.country}(${member.timezone})`
                        : member.country
                      : "-"}
                  </td>
                  <td className="px-4 py-3 text-right">
                    <MemberActionsMenu
                      isOpen={openMenuId === member.memberId}
                      onToggle={() =>
                        setOpenMenuId((current) => (current === member.memberId ? null : member.memberId))
                      }
                      canEditRole={!member.isAiTeammate}
                      onEditRole={() => startEditingRole(member)}
                      onDelete={() => handleRemove(member.memberId, memberDisplayName(member))}
                    />
                  </td>
                </tr>
              ))}
              {members.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-4 py-8 text-center text-muted">
                    팀원이 없어요.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
