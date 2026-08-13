/**
 * 프로젝트 상세 > 연동 상태 탭
 *
 * 사용 예시:
 * <IntegrationTab />
 *
 * 디자인 확정 시 교체 필요: 서비스 아이콘은 회색 사각형 플레이스홀더입니다.
 *
 * 명세의 GET /integrations/status는 전역 단위이나 이 화면은 프로젝트별 상태를
 * 표시합니다. (백엔드 확인 필요)
 */

import Badge from "../../../../components/Badge.jsx";
import {
  integrations,
  integrationLabel,
  integrationStatusLabel,
  integrationStatusVariant,
  syncLogs,
} from "../../../../api/mock/projectDetailData.js";

export default function IntegrationTab() {
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
              <Badge variant={integrationStatusVariant[integration.status]}>
                {integrationStatusLabel[integration.status]}
              </Badge>
            </div>
            <p className="mt-4 text-xs text-muted">
              마지막 동기화: {integration.lastSyncedAt}
            </p>
          </section>
        ))}
      </div>

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
