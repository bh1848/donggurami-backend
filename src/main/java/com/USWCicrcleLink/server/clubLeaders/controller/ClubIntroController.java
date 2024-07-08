package com.USWCicrcleLink.server.clubLeaders.controller;

import com.USWCicrcleLink.server.clubLeaders.dto.ClubIntroRequest;
import com.USWCicrcleLink.server.clubLeaders.dto.ClubIntroResponse;
import com.USWCicrcleLink.server.clubLeaders.service.ClubIntroService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs")
public class ClubIntroController {

    private final ClubIntroService clubIntroService;

    //동아리 소개글 조회
    @GetMapping("/{clubId}/clubIntro")
    public ResponseEntity<ApiResponse<ClubIntroResponse>> getClubIntroByClubId(@PathVariable("clubId") Long id) {
        ClubIntroResponse clubIntroResponse = clubIntroService.getClubIntroByClubId(id);
        ApiResponse<ClubIntroResponse> response = new ApiResponse<>("동아리 소개글 조회 성공", clubIntroResponse);
        return ResponseEntity.ok(response);
    }

    //지원서 작성 페이지로 이동
    @GetMapping("/{clubId}/apply")
    public ResponseEntity<ApiResponse<String>> showApplyPage(@PathVariable("clubId") Long id) {
        ClubIntroResponse clubIntroResponse = clubIntroService.getClubIntroByClubId(id);
        ApiResponse<String> response = new ApiResponse<>("지원 페이지 이동 성공", clubIntroResponse.getGoogleFormUrl());
        return ResponseEntity.ok(response);
    }

    //구글 폼으로 이동
    @GetMapping("/{clubId}/apply/form")
    public ResponseEntity<Void> applyToClub(@PathVariable("clubId") Long id) {
        ClubIntroResponse clubIntroResponse = clubIntroService.getClubIntroByClubId(id);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", clubIntroResponse.getGoogleFormUrl())
                .build();
    }

    @PostMapping("/save")
    public ResponseEntity<Boolean> setClubInfo(@Validated ClubIntroRequest clubInfoRequest) throws IOException {
        clubIntroService.writeClubIntro(clubInfoRequest);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}
