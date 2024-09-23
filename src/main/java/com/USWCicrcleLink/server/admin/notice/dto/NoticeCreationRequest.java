package com.USWCicrcleLink.server.admin.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoticeCreationRequest {

    @NotBlank(message = "제목은 비워둘 수 없습니다.")
    @Size(max = 100, message = "제목은 최대 100자까지 입력 가능합니다.")
    private String noticeTitle;

    @NotBlank(message = "내용은 비워둘 수 없습니다.")
    @Size(max = 1000, message = "내용은 최대 1000자까지 입력 가능합니다.")
    private String noticeContent;

    @Builder.Default
    private List<Integer> photoOrders = new ArrayList<>(); // 사진 순서 목록
}