package com.USWCicrcleLink.server.club.clubIntro.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class IntroPhotoUploadRequest {
    private int order; // 순서를 나타내는 필드
    private MultipartFile introPhoto; // 사진 파일
}
