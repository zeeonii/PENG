/**
 * 설정 > 연동 관리
 *
 * 디자인 확정 시 교체 필요: 서비스 아이콘은 회색 사각형 플레이스홀더입니다.
 *
 * 재연결은 현재 mock 동작이며, 실제 연동 시
 * POST /integrations/notion, /integrations/google-meet 로 교체합니다.
 */

import SettingsLayout from "../SettingsLayout.jsx";
import Badge from "../../../components/Badge.jsx";
import Button from "../../../components/Button.jsx";
import { settingIntegrations } from "../../../api/mock/settingsData.js";
import {
  integrationLabel,
  integrationStatusLabel,
  integrationStatusVariant,
} from "../../../api/mock/projectDetailData.js";

export default function IntegrationManagePage() {
  return (
    <SettingsLayout>
      <h1 className="text-2xl font-bold text-primary">연동 관리</h1>

      <div className="mt-6 space-y-4">
        {settingIntegrations.map((integration) => (
          <section
            key={integration.type}
            className="rounded-xl border border-border bg-white p-5"
          >
            <div className="flex flex-wrap items-center justify-between gap-3">
              <span className="flex items-center gap-3">
                {/* 디자인 확정 시 서비스 아이콘으로 교체 */}
                <span className="h-9 w-9 shrink-0 rounded bg-secondary" />
                <span>
                  <strong className="block font-bold text-primary">
                    {integrationLabel[integration.type]}
                  </strong>
                  <small className="text-xs text-muted">
                    {integration.description}
                  </small>
                </span>
              </span>

              <span className="flex items-center gap-3">
                <Badge variant={integrationStatusVariant[integration.status]}>
                  {integration.status === "DISCONNECTED"
                    ? "제한"
                    : integrationStatusLabel[integration.status]}
                </Badge>
                <Button variant="secondary">재연결</Button>
              </span>
            </div>

            {integration.notice && (
              <p className="mt-4 border-t border-border pt-4 text-xs leading-5 text-muted">
                {integration.notice}
              </p>
            )}

            {integration.lastSyncedAt && (
              <p className="mt-4 text-xs text-muted">
                마지막 동기화: {integration.lastSyncedAt}
              </p>
            )}
          </section>
        ))}
      </div>
    </SettingsLayout>
  );
}
