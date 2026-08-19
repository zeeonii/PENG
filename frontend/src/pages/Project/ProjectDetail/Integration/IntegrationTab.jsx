/**
 * 프로젝트 상세 > 연동 상태 탭
 *
 * 사용 예시:
 * <IntegrationTab projectId={project.id} />
 *
 * 디자인 확정 시 교체 필요: 서비스 아이콘은 회색 사각형 플레이스홀더입니다.
 */

import { useEffect, useState } from "react";
import Badge from "../../../../components/Badge.jsx";
import { getIntegrationStatus } from "../../../../api/integration.js";
import { syncLogs } from "../../../../api/mock/projectDetailData.js";

const integrationLabel = { NOTION: "Notion", GOOGLE_MEET: "Google Meet" };
const statusLabel = { CONNECTED: "정상", DISCONNECTED: "연결 끊김" };
const statusVariant = { CONNECTED: "success", DISCONNECTED: "danger" };

function formatDateTime(value) {
  if (!value) return "동기화 기록 없음";
  return new Date(value).toLocaleString("ko-KR");
}

export default function IntegrationTab({ projectId }) {
  const [integrations, setIntegrations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let ignore = false;

    getIntegrationStatus(projectId)
      .then(({ data }) => {
        if (!ignore) setIntegrations(data);
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

  if (loading) return <p className="text-sm text-muted">불러오는 중...</p>;
  if (error) return <p className="text-sm text-danger">연동 상태를 불러오지 못했어요.</p>;

  return (
    <div>
      <div className="grid gap-4 sm:grid-cols-2">
        {integrations.map((integration) => (
          <section
            key={integration.type}
            className="rounded-xl border border-border bg-white p-5"
          >
            <div className="flex items-center justify-between gap-3">
              <span className="flex items-center gap-3">
                {/* 디자인 확정 시 서비스 아이콘으로 교체 */}
                <span className="h-8 w-8 shrink-0 rounded bg-secondary" />
                <strong className="font-bold text-primary">
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
