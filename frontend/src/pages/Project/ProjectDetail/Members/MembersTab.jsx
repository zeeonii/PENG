/**
 * 프로젝트 상세 > 팀원 관리 탭
 *
 * 사용 예시:
 * <MembersTab />
 *
 * 팀원 초대/삭제는 현재 mock 동작이며, 실제 연동 시
 * POST/DELETE /projects/{projectId}/members 로 교체합니다.
 */

import { useState } from "react";
import Avatar from "../../../../components/Avatar.jsx";
import Badge from "../../../../components/Badge.jsx";
import Button from "../../../../components/Button.jsx";
import {
  projectMembers,
  roleLabel,
} from "../../../../api/mock/projectDetailData.js";

const ROLE_VARIANT = { AUTO: "active", ADMIN: "default", MEMBER: "default" };

export default function MembersTab() {
  const [members, setMembers] = useState(projectMembers);

  const handleRemove = (memberId) => {
    setMembers((prev) => prev.filter((member) => member.memberId !== memberId));
  };

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h3 className="text-lg font-bold text-primary">
          팀원 ({members.length})
        </h3>
        <Button variant="secondary">＋ 팀원 초대</Button>
      </div>

      <div className="overflow-x-auto rounded-xl border border-border bg-white">
        <table className="w-full min-w-[560px] text-left text-sm">
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
                  <span className="flex items-center gap-2">
                    <Avatar name={member.name} size="sm" />
                    <strong className="font-medium text-primary">
                      {member.name}
                    </strong>
                  </span>
                </td>
                <td className="px-4 py-3 text-muted">{member.duty}</td>
                <td className="px-4 py-3 text-muted">
                  {member.timezone
                    ? `${member.country}(${member.timezone})`
                    : member.country}
                </td>
                <td className="px-4 py-3">
                  <Badge variant={ROLE_VARIANT[member.role]}>
                    {roleLabel[member.role]}
                  </Badge>
                </td>
                <td className="px-4 py-3 text-right">
                  {member.role !== "AUTO" && (
                    <button
                      type="button"
                      onClick={() => handleRemove(member.memberId)}
                      aria-label={`${member.name} 삭제`}
                      className="text-muted hover:text-danger"
                    >
                      ✕
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
