package com.USWCicrcleLink.server.club.clubIntro.api;

import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.club.dto.ClubByRecruitmentStatusResponse;
import com.USWCicrcleLink.server.club.club.dto.ClubListResponse;
import com.USWCicrcleLink.server.club.clubIntro.dto.ClubIntroResponse;
import com.USWCicrcleLink.server.club.clubIntro.service.ClubIntroService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs")
public class ClubIntroController {

    private final ClubIntroService clubIntroService;

    //분과별 동아리 조회(모바일)
    @GetMapping()
    public ResponseEntity<ApiResponse<List<ClubListResponse>>> getClubsByDepartment() {
        List<ClubListResponse> clubs = clubIntroService.getAllClubs();
        ApiResponse<List<ClubListResponse>> response = new ApiResponse<>("동아리 리스트 조회 성공", clubs);
        return ResponseEntity.ok(response);
    }

    //모집 상태 및 분과별 동아리 조회(모바일)
    @GetMapping("/{recruitmentStatus}")
    public ResponseEntity<ApiResponse<List<ClubByRecruitmentStatusResponse>>> getClubsByRecruitmentStatus(
            @PathVariable("recruitmentStatus") RecruitmentStatus recruitmentStatus) {
        List<ClubByRecruitmentStatusResponse> clubs = clubIntroService.getClubsByRecruitmentStatus(recruitmentStatus);
        ApiResponse<List<ClubByRecruitmentStatusResponse>> response = new ApiResponse<>("모집 상태별 동아리 리스트 조회 성공", clubs);
        return ResponseEntity.ok(response);
    }

    //동아리 소개글 조회(모바일)
    @GetMapping("/intro/{clubId}")
    public ResponseEntity<ApiResponse<ClubIntroResponse>> getClubIntroByClubId(@PathVariable("clubId") Long id) {
        ClubIntroResponse clubIntroResponse = clubIntroService.getClubIntro(id);
        ApiResponse<ClubIntroResponse> response = new ApiResponse<>("동아리 소개글 조회 성공", clubIntroResponse);
        return ResponseEntity.ok(response);
    }
}