/**
 * 프로젝트 상세 > 팀원 관리 탭
 *
 * 사용 예시:
 * <MembersTab projectId={project.id} />
 *
 * 명세에 AI 팀원 여부를 나타내는 필드가 없어, 삭제 불가 여부는 미리 판단하지 않고
 * 백엔드가 거부(예: 생성자 삭제 시도)했을 때 에러 메시지로 안내합니다.
 */

import { useEffect, useState } from "react";
import Avatar from "../../../../components/Avatar.jsx";
import Button from "../../../../components/Button.jsx";
import Input from "../../../../components/Input.jsx";
import {
  getProjectMembers,
  inviteProjectMember,
  removeProjectMember,
} from "../../../../api/project.js";

export default function MembersTab({ projectId }) {
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [inviting, setInviting] = useState(false);
  const [inviteEmail, setInviteEmail] = useState("");
  const [inviteError, setInviteError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

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
    try {
      await removeProjectMember(projectId, memberId);
      setMembers((prev) => prev.filter((member) => member.memberId !== memberId));
    } catch {
      setError(new Error(`${name}님을 내보낼 수 없어요. (프로젝트 생성자는 내보낼 수 없습니다)`));
    }
  };

  const handleInvite = async (event) => {
    event.preventDefault();
    const email = inviteEmail.trim();
    if (!email || submitting) return;

    setSubmitting(true);
    setInviteError(null);
    try {
      await inviteProjectMember(projectId, { email });
      const { data } = await getProjectMembers(projectId);
      setMembers(data);
      setInviteEmail("");
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
          <Button type="submit" disabled={submitting}>
            {submitting ? "초대 중..." : "초대"}
          </Button>
        </form>
      )}
      {inviteError && <p className="mb-4 text-sm text-danger">{inviteError}</p>}

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
                <th className="px-4 py-3 font-medium">담당 업무</th>
                <th className="px-4 py-3 font-medium">국가 · 시간대</th>
                <th className="px-4 py-3 font-medium">역할</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {members.map((member) => (
                <tr key={member.memberId}>
                  <td className="px-4 py-3">
                    <span className="flex items-center gap-2 overflow-hidden">
                      <Avatar name={member.name} size="sm" />
                      <strong className="truncate font-medium text-primary">
                        {member.name}
                      </strong>
                    </span>
                  </td>
                  <td className="truncate px-4 py-3 text-muted">
                    {member.role ?? "-"}
                  </td>
                  <td className="truncate px-4 py-3 text-muted">
                    {member.country
                      ? member.timezone
                        ? `${member.country}(${member.timezone})`
                        : member.country
                      : "-"}
                  </td>
                  <td className="px-4 py-3 text-muted">-</td>
                  <td className="px-4 py-3 text-right">
                    <button
                      type="button"
                      onClick={() => handleRemove(member.memberId, member.name)}
                      aria-label={`${member.name} 삭제`}
                      className="text-muted hover:text-danger"
                    >
                      ✕
                    </button>
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
