package com.USWCicrcleLink.server.aplict.controller;

import com.USWCicrcleLink.server.aplict.dto.AplictRequest;
import com.USWCicrcleLink.server.aplict.service.AplictService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/aplict")
public class AplictController {
    private final AplictService aplictService;

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
