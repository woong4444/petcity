package com.jjang051.petcity.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatSendRequestDto {
    /*
        ^[0-9a-fA-F]{8}-        # 첫 번째 블록: 16진수 8자리
        [0-9a-fA-F]{4}-         # 두 번째 블록: 16진수 4자리
        [1-5][0-9a-fA-F]{3}-    # 세 번째 블록: 첫 글자는 1~5, 나머지 3자리 16진수
        [89abAB][0-9a-fA-F]{3}- # 네 번째 블록: 첫 글자는 8,9,a,b 중 하나, 나머지 3자리 16진수
        [0-9a-fA-F]{12}$        # 마지막 블록: 16진수 12자리
     */

    @NotBlank(message = "채팅방 정보가 없습니다.")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-" + "[0-9a-fA-F]{4}-"
            + "[1-5][0-9a-fA-F]{3}-" + "[89abAB][0-9a-fA-F]{3}-"
            + "[0-9a-fA-F]{12}$", message = "채팅방 정보가 올바르지 않습니다")
   private String roomUuid;

    @NotBlank(message = "메시지 값이 없습니당")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-" + "[0-9a-fA-F]{4}-"
            + "[1-5][0-9a-fA-F]{3}-" + "[89abAB][0-9a-fA-F]{3}-"
            + "[0-9a-fA-F]{12}$", message = "메시지 값이 올바르지 않습니다")
    private String clientMessageUuid;

    @NotBlank(message = "메시지를 입력해 주세요.")
    @Size(max = 500, message = "메시지는 최대 500글자까지 입력할 수 있습니다")
    private String content;
}
