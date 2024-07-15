package com.USWCicrcleLink.server.aplict.api;

import com.USWCicrcleLink.server.aplict.dto.AplictRequest;
import com.USWCicrcleLink.server.aplict.service.AplictService;
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

    //지원서 작성하기(구글 폼 이동)
    @GetMapping("/{clubId}")
    public ResponseEntity<Void> applyToClub(@PathVariable("clubId") Long clubId) {
        String googleFormUrl = aplictService.getGoogleFormUrlByClubId(clubId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", googleFormUrl)
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