package com.USWCicrcleLink.server.club.api;

import com.USWCicrcleLink.server.club.domain.Department;
import com.USWCicrcleLink.server.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.dto.ClubByDepartmentResponse;
import com.USWCicrcleLink.server.club.dto.ClubIntroRequest;
import com.USWCicrcleLink.server.club.dto.ClubIntroResponse;
import com.USWCicrcleLink.server.club.service.ClubIntroService;
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
public class ClubIntroController {

    private final ClubIntroService clubIntroService;

    //분과별 동아리 조회
    @GetMapping("/department/{department}")
    public ResponseEntity<ApiResponse<List<ClubByDepartmentResponse>>> getClubsByDepartment(@PathVariable("department") Department department) {
        List<ClubByDepartmentResponse> clubs = clubIntroService.getClubsByDepartment(department);
        ApiResponse<List<ClubByDepartmentResponse>> response = new ApiResponse<>("분과별 동아리 조회 성공", clubs);
        return ResponseEntity.ok(response);
    }

    //모집 상태 및 분과별 동아리 조회
    @GetMapping("/department/{department}/{recruitmentStatus}")
    public ResponseEntity<ApiResponse<List<ClubByDepartmentResponse>>> getClubsByRecruitmentStatusAndDepartment(
            @PathVariable("department") Department department,
            @PathVariable("recruitmentStatus") RecruitmentStatus recruitmentStatus) {
        List<ClubByDepartmentResponse> clubs = clubIntroService.getClubsByRecruitmentStatusAndDepartment(recruitmentStatus, department);
        ApiResponse<List<ClubByDepartmentResponse>> response = new ApiResponse<>("모집 상태 및 분과별 동아리 조회 성공", clubs);
        return ResponseEntity.ok(response);
    }

    //동아리 소개글 조회
    @GetMapping("/{clubId}/clubIntro")
    public ResponseEntity<ApiResponse<ClubIntroResponse>> getClubIntroByClubId(@PathVariable("clubId") Long id) {
        ClubIntroResponse clubIntroResponse = clubIntroService.getClubIntroByClubId(id);
        ApiResponse<ClubIntroResponse> response = new ApiResponse<>("동아리 소개글 조회 성공", clubIntroResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/save")
    public ResponseEntity<Boolean> setClubInfo(@Validated ClubIntroRequest clubIntroRequest) throws IOException {
        clubIntroService.writeClubIntro(clubIntroRequest);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
}