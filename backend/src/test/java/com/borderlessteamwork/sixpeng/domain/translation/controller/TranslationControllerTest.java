package com.borderlessteamwork.sixpeng.domain.translation.controller;

import com.borderlessteamwork.sixpeng.domain.translation.dto.Language;
import com.borderlessteamwork.sixpeng.domain.translation.dto.response.TranslationResponse;
import com.borderlessteamwork.sixpeng.domain.translation.service.TranslationService;
import com.borderlessteamwork.sixpeng.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TranslationController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.context.annotation.Import(GlobalExceptionHandler.class)
class TranslationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    TranslationService translationService;

    @Test
    void 번역_요청이_성공하면_200과_결과를_반환한다() throws Exception {
        when(translationService.translate(any())).thenReturn(TranslationResponse.of("안녕", "Hi"));

        mockMvc.perform(post("/translation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TranslationRequestFixture("안녕", Language.KR, Language.EN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.original").value("안녕"))
                .andExpect(jsonPath("$.translated").value("Hi"));
    }

    @Test
    void text가_비어있으면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/translation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TranslationRequestFixture("", null, null))))
                .andExpect(status().isBadRequest());
    }

    private record TranslationRequestFixture(String text, Language senderLanguage, Language receiverLanguage) {
    }
}
