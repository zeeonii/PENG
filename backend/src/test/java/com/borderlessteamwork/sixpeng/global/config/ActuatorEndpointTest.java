package com.borderlessteamwork.sixpeng.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ActuatorEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("/actuator/health 는 로그인 없이 열려 있고 상태만 내려준다")
    void healthIsPublicAndHidesDetails() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                // show-details: never 라 DB 등 구성 요소 정보가 새지 않아야 한다.
                .andExpect(jsonPath("$.components").doesNotExist());
    }

    @Test
    @DisplayName("/actuator/info 는 배포된 커밋과 빌드 시각을 알려준다")
    void infoExposesDeploymentAndBuildInfo() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                // 이미지 빌드 인자(DEPLOY_COMMIT/DEPLOY_BRANCH) 기반.
                // 로컬/CI 에서는 unknown 이지만 키는 항상 존재해야 한다.
                .andExpect(jsonPath("$.deployment.commit").exists())
                .andExpect(jsonPath("$.deployment.branch").exists())
                // build-info.properties 는 Gradle 의 bootBuildInfo 가 생성한다.
                .andExpect(jsonPath("$.build.time").exists())
                .andExpect(jsonPath("$.build.artifact").value("sixpeng"));
    }

    @Test
    @DisplayName("노출하지 않은 actuator 엔드포인트는 접근할 수 없다")
    void otherActuatorEndpointsAreNotExposed() throws Exception {
        // env 는 환경변수(시크릿 포함)를 그대로 노출하므로 절대 열려 있으면 안 된다.
        mockMvc.perform(get("/actuator/env"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/actuator/beans"))
                .andExpect(status().isUnauthorized());
    }
}
