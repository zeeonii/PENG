package com.borderlessteamwork.sixpeng.domain.message.controller;

import com.borderlessteamwork.sixpeng.domain.message.dto.request.MessageCreateRequest;
import com.borderlessteamwork.sixpeng.domain.message.dto.response.MessageResponse;
import com.borderlessteamwork.sixpeng.domain.message.entity.Message;
import com.borderlessteamwork.sixpeng.domain.message.service.MessageService;
import com.borderlessteamwork.sixpeng.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class MessageControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    MessageService messageService;

    @Test
    void X_Member_Id_헤더가_없으면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/messages"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 내_쪽지_목록을_조회한다() throws Exception {
        Message message = Message.create(1L, 10L, 20L, "hi", "안녕");
        when(messageService.getMessages(10L)).thenReturn(List.of(MessageResponse.from(message)));

        mockMvc.perform(get("/messages").header("X-Member-Id", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].originalText").value("hi"));
    }

    @Test
    void 쪽지를_전송하면_201을_반환한다() throws Exception {
        Message message = Message.create(1L, 10L, 20L, "hi", "안녕");
        when(messageService.sendMessage(eq(10L), any())).thenReturn(MessageResponse.from(message));

        MessageCreateRequest request = new MessageCreateRequest();
        request.setProjectId(1L);
        request.setReceiverId(20L);
        request.setOriginalText("hi");

        mockMvc.perform(post("/messages")
                        .header("X-Member-Id", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.senderId").value(10))
                .andExpect(jsonPath("$.isRead").value(false));
    }

    @Test
    void originalText가_비어있으면_400을_반환한다() throws Exception {
        MessageCreateRequest request = new MessageCreateRequest();
        request.setProjectId(1L);
        request.setReceiverId(20L);
        request.setOriginalText("");

        mockMvc.perform(post("/messages")
                        .header("X-Member-Id", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 쪽지를_읽음처리하면_200을_반환한다() throws Exception {
        mockMvc.perform(patch("/messages/{messageId}/read", 1L))
                .andExpect(status().isOk());
    }
}
