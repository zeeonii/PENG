package com.borderlessteamwork.sixpeng.domain.message.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessageCreateRequest {

    @NotNull
    private Long projectId;

    @NotNull
    private Long receiverId;

    @NotBlank
    private String originalText;
}
