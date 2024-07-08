package com.USWCicrcleLink.server.club.api;

import com.USWCicrcleLink.server.aplict.dto.AplictResponse;
import com.USWCicrcleLink.server.club.dto.ClubInfoRequest;
import com.USWCicrcleLink.server.club.service.ClubService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs")
public class ClubController {
    private final ClubService clubService;

    //해당 동아리 지원서 조회
    @GetMapping("/aplict/{clubId}")
    public ResponseEntity<ApiResponse<List<AplictResponse>>> getAplictByClubId(@PathVariable("clubId") Long clubId) {
        List<AplictResponse> aplicts = clubService.getAplictByClubId(clubId);
        ApiResponse<List<AplictResponse>> response = new ApiResponse<>("지원서 조회 성공", aplicts);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/update")
    public ResponseEntity<Boolean> updateClubInfo(@Validated ClubInfoRequest clubInfoRequest) throws IOException {
        clubService.updateClubInfo(clubInfoRequest);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}