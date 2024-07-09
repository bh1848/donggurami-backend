package com.USWCicrcleLink.server.aplict.api;

import com.USWCicrcleLink.server.aplict.dto.AplictRequest;
import com.USWCicrcleLink.server.aplict.service.AplictService;
import com.USWCicrcleLink.server.club.dto.ClubIntroResponse;
import com.USWCicrcleLink.server.club.service.ClubIntroService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/aplict")
public class AplictController {
    private final AplictService aplictService;
    private final ClubIntroService clubIntroService;

    //지원서 작성 페이지로 이동
    @GetMapping("/{clubId}")
    public ResponseEntity<ApiResponse<String>> showApplyPage(@PathVariable("clubId") Long id) {
        ClubIntroResponse clubIntroResponse = clubIntroService.getClubIntroByClubId(id);
        ApiResponse<String> response = new ApiResponse<>("지원 페이지 이동 성공", clubIntroResponse.getGoogleFormUrl());
        return ResponseEntity.ok(response);
    }

    //구글 폼으로 이동
    @GetMapping("/{clubId}/form")
    public ResponseEntity<Void> applyToClub(@PathVariable("clubId") Long id) {
        ClubIntroResponse clubIntroResponse = clubIntroService.getClubIntroByClubId(id);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", clubIntroResponse.getGoogleFormUrl())
                .build();
    }

    //동아리 지원서 제출
    @PostMapping("/submit/{clubId}")
    public ResponseEntity<ApiResponse<Void>> submitAplict(
            @RequestHeader("User-uuid") UUID userUUID,
            @PathVariable("clubId") Long clubId,
            @RequestBody AplictRequest request) {
        aplictService.submitAplict(userUUID, clubId, request);
        ApiResponse<Void> response = new ApiResponse<>("지원서 제출 성공");
        return ResponseEntity.ok(response);
    }
}