package com.borderlessteamwork.sixpeng.domain.integration.scheduler;

import com.borderlessteamwork.sixpeng.domain.integration.service.IntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 사용자가 매번 재연동하지 않아도 Notion 페이지 수정 내용이 자동으로 반영되도록,
 * 연동된 모든 프로젝트를 주기적으로 다시 훑어 문서를 갱신한다.
 *
 * <p>즉시 반영되는 웹훅 대신 폴링을 택한 이유: 이 프로젝트에서 수정 반영까지의
 * 실시간성이 핵심 요구사항이 아니고, 웹훅은 구독 등록/검증/서명 확인 등 구현·운영
 * 복잡도가 커서 이 일정에는 폴링이 더 실용적이라고 판단했다.
 *
 * <p>테스트에서는 이 빈 자체를 비활성화한다. {@code @Scheduled(fixedRate=...)}는
 * initialDelay를 안 주면 컨텍스트가 뜨자마자 즉시 한 번 실행되는데, 그러면 다른
 * {@code @SpringBootTest}가 IntegrationService를 mock/stub하는 도중에 스케줄러
 * 스레드가 같은 mock의 다른 메서드를 동시에 호출해 Mockito 스텁 상태를 깨뜨린다.
 */
@Component
@ConditionalOnProperty(value = "app.scheduling.notion-sync.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class NotionSyncScheduler {

    private static final long FIFTEEN_MINUTES_MS = 15 * 60 * 1000L;

    private final IntegrationService integrationService;

    @Scheduled(fixedRate = FIFTEEN_MINUTES_MS)
    public void resyncNotion() {
        integrationService.resyncAllNotionConnections();
    }
}
