/**
 * 설정 > 연동 관리
 *
 * 디자인 확정 시 교체 필요: 서비스 아이콘은 회색 사각형 플레이스홀더입니다.
 *
 * ⚠️ 이 화면의 라우트(/settings)에는 프로젝트 정보가 없는데, 연동 상태·시작 API는
 * 프로젝트 단위입니다. 임시로 내가 참여한 첫 번째 프로젝트를 기준으로 표시합니다.
 * 근본적으로는 라우트를 /projects/:projectId/settings 형태로 바꾸거나,
 * 프로젝트 선택 UI를 추가하는 게 맞아 보입니다. (팀 논의 필요)
 */

import { useEffect, useState } from "react";
import SettingsLayout from "../SettingsLayout.jsx";
import Badge from "../../../components/Badge.jsx";
import { getProjects } from "../../../api/project.js";
import { getIntegrationStatus } from "../../../api/integration.js";

const ALL_INTEGRATION_TYPES = ["GOOGLE_MEET", "NOTION"];
const integrationLabel = { NOTION: "Notion", GOOGLE_MEET: "Google Meet" };
const statusLabel = { CONNECTED: "정상", DISCONNECTED: "연결 끊김" };
const statusVariant = { CONNECTED: "success", DISCONNECTED: "danger" };

// 백엔드는 한 번도 연동을 시도하지 않은 서비스는 목록에서 아예 빼고 내려주므로,
// 연동 안 된 서비스도 항상 보이도록 전체 서비스 목록 기준으로 채워 넣습니다.
function withAllIntegrationTypes(integrations) {
  return ALL_INTEGRATION_TYPES.map(
    (type) =>
      integrations.find((integration) => integration.type === type) ?? {
        type,
        status: "DISCONNECTED",
        lastSyncedAt: null,
      },
  );
}

function formatDateTime(value) {
  if (!value) return "동기화 기록 없음";
  return new Date(value).toLocaleString("ko-KR");
}

export default function IntegrationManagePage() {
  const [projectId, setProjectId] = useState(null);
  const [integrations, setIntegrations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let ignore = false;

    getProjects()
      .then(({ data }) => {
        if (ignore) return;
        const firstProject = data[0];
        if (!firstProject) {
          setLoading(false);
          return;
        }
        setProjectId(firstProject.id);
        return getIntegrationStatus(firstProject.id).then(({ data: statusData }) => {
          if (!ignore) setIntegrations(withAllIntegrationTypes(statusData));
        });
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

  return (
    <SettingsLayout>
      <h1 className="text-2xl font-bold text-primary">연동 관리</h1>

      {loading && <p className="mt-6 text-sm text-muted">불러오는 중...</p>}
      {error && <p className="mt-6 text-sm text-danger">{error.message}</p>}
      {!loading && !projectId && (
        <p className="mt-6 text-sm text-muted">참여 중인 프로젝트가 없어요.</p>
      )}

      {!loading && projectId && (
        <div className="mt-6 space-y-4">
          {integrations.map((integration) => (
            <section
              key={integration.type}
              className="rounded-xl border border-border bg-white p-5"
            >
              <div className="flex flex-wrap items-center justify-between gap-3">
                <span className="flex items-center gap-3">
                  {/* 디자인 확정 시 서비스 아이콘으로 교체 */}
                  <span className="h-9 w-9 shrink-0 rounded bg-secondary" />
                  <strong className="block font-bold text-primary">
                    {integrationLabel[integration.type]}
                  </strong>
                </span>

                <Badge variant={statusVariant[integration.status]}>
                  {statusLabel[integration.status]}
                </Badge>
              </div>

              <p className="mt-4 text-xs text-muted">
                마지막 동기화: {formatDateTime(integration.lastSyncedAt)}
              </p>
            </section>
          ))}
          {integrations.length === 0 && (
            <p className="text-sm text-muted">연동된 서비스가 없어요.</p>
          )}
        </div>
      )}
    </SettingsLayout>
  );
}
