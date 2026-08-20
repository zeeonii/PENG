/**
 * 프로젝트 상세 > 연동 상태 탭
 *
 * 사용 예시:
 * <IntegrationTab projectId={project.id} />
 */

import { useEffect, useState } from "react";
import Badge from "../../../../components/Badge.jsx";
import Button from "../../../../components/Button.jsx";
import GoogleMeetIcon from "../../../../components/icons/GoogleMeetIcon.jsx";
import NotionIcon from "../../../../components/icons/NotionIcon.jsx";
import {
  getIntegrationStatus,
  connectNotion,
  connectGoogleMeet,
} from "../../../../api/integration.js";
import { syncLogs } from "../../../../api/mock/projectDetailData.js";

const ALL_INTEGRATION_TYPES = ["GOOGLE_MEET", "NOTION"];
const integrationLabel = { NOTION: "Notion", GOOGLE_MEET: "Google Meet" };
const integrationIcon = { NOTION: NotionIcon, GOOGLE_MEET: GoogleMeetIcon };
const connectIntegration = { NOTION: connectNotion, GOOGLE_MEET: connectGoogleMeet };
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

export default function IntegrationTab({ projectId }) {
  const [integrations, setIntegrations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [connecting, setConnecting] = useState(null);
  const [connectError, setConnectError] = useState(null);

  useEffect(() => {
    let ignore = false;

    getIntegrationStatus(projectId)
      .then(({ data }) => {
        if (!ignore) setIntegrations(withAllIntegrationTypes(data));
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

  const handleConnect = async (type) => {
    setConnecting(type);
    setConnectError(null);
    try {
      await connectIntegration[type](projectId);
    } catch {
      setConnectError("연동을 시작하지 못했어요.");
      setConnecting(null);
    }
  };

  if (loading) return <p className="text-sm text-muted">불러오는 중...</p>;
  if (error) return <p className="text-sm text-danger">연동 상태를 불러오지 못했어요.</p>;

  return (
    <div>
      {connectError && <p className="mb-4 text-sm text-danger">{connectError}</p>}
      <div className="grid gap-4 sm:grid-cols-2">
        {integrations.map((integration) => {
          const Icon = integrationIcon[integration.type];
          return (
            <section
              key={integration.type}
              className="rounded-xl border border-border bg-white p-5"
            >
              <div className="flex items-center justify-between gap-3">
                <span className="flex items-center gap-3">
                  <Icon className="h-8 w-8 shrink-0" />
                  <strong className="font-bold text-primary">
                    {integrationLabel[integration.type]}
                  </strong>
                </span>
                <Badge variant={statusVariant[integration.status]}>
                  {statusLabel[integration.status]}
                </Badge>
              </div>
              <div className="mt-4 flex items-center justify-between gap-3">
                <p className="text-xs text-muted">
                  마지막 동기화: {formatDateTime(integration.lastSyncedAt)}
                </p>
                <Button
                  variant="secondary"
                  className="shrink-0 px-3 py-1.5 text-xs"
                  onClick={() => handleConnect(integration.type)}
                  disabled={connecting === integration.type}
                >
                  {connecting === integration.type ? "연결 중..." : "재연동"}
                </Button>
              </div>
            </section>
          );
        })}
        {integrations.length === 0 && (
          <p className="text-sm text-muted">연동된 서비스가 없어요.</p>
        )}
      </div>

      {/* 동기화 로그 조회 API가 명세에 없어 아직 mock입니다. */}
      <section className="mt-8">
        <h3 className="text-lg font-bold text-primary">동기화 로그</h3>
        <ul className="mt-3 divide-y divide-border rounded-xl border border-border bg-white">
          {syncLogs.map((log) => (
            <li
              key={log.id}
              className="flex flex-wrap items-center justify-between gap-2 px-4 py-3 text-sm"
            >
              <span className="text-primary">{log.message}</span>
              <span className="text-xs text-muted">{log.occurredAt}</span>
            </li>
          ))}
        </ul>
      </section>
    </div>
  );
}
