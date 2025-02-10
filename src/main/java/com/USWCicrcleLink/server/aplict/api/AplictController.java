package com.USWCicrcleLink.server.aplict.api;

import com.USWCicrcleLink.server.aplict.dto.AplictRequest;
import com.USWCicrcleLink.server.aplict.service.AplictService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/apply")
public class AplictController {
    private final AplictService aplictService;

    // 지원 가능 여부 확인 (모바일)
    @GetMapping("/can-apply/{clubId}")
    public ResponseEntity<ApiResponse<Boolean>> canApply(@PathVariable("clubId") Long clubId) {
        aplictService.checkIfCanApply(clubId);
        return ResponseEntity.ok(new ApiResponse<>("지원 가능"));
    }

    //구글 폼 URL 조회 (모바일)
    @GetMapping("/{clubId}")
    public ResponseEntity<ApiResponse<String>> getGoogleFormUrl(@PathVariable("clubId") Long clubId) {
        String googleFormUrl = aplictService.getGoogleFormUrlByClubId(clubId);
        return ResponseEntity.ok(new ApiResponse<>("구글 폼 URL 조회 성공", googleFormUrl));
    }

    //동아리 지원서 제출 (모바일)
    @PostMapping("/{clubId}")
    public ResponseEntity<ApiResponse<Void>> submitAplict(
            @PathVariable("clubId") Long clubId,
            @RequestBody @Valid AplictRequest request) {
        aplictService.submitAplict(clubId, request);
        return ResponseEntity.ok(new ApiResponse<>("지원서 제출 성공"));
    }
}