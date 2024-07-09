package com.USWCicrcleLink.server.club.controller;

import com.USWCicrcleLink.server.aplict.dto.AplictResponse;
import com.USWCicrcleLink.server.club.domain.Department;
import com.USWCicrcleLink.server.club.dto.ClubByDepartmentResponse;
import com.USWCicrcleLink.server.clubLeader.dto.ClubInfoRequest;
import com.USWCicrcleLink.server.club.dto.ClubResponse;
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

    //모든 동아리 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClubResponse>>> getAllClubs() {
        List<ClubResponse> clubs = clubService.getAllClubs();
        ApiResponse<List<ClubResponse>> response = new ApiResponse<>("모든 동아리 조회 성공", clubs);
        return ResponseEntity.ok(response);
    }

    //동아리 조회
    @GetMapping("/{clubId}")
    public ResponseEntity<ApiResponse<ClubResponse>> getClubById(@PathVariable("clubId") Long id) {
        ClubResponse club = clubService.getClubById(id);
        ApiResponse<ClubResponse> response = new ApiResponse<>("동아리 조회 성공", club);
        return ResponseEntity.ok(response);
    }

    //분과별 동아리 조회
    @GetMapping("/department/{department}")
    public ResponseEntity<ApiResponse<List<ClubByDepartmentResponse>>> getClubsByDepartment(@PathVariable("department") Department department) {
        List<ClubByDepartmentResponse> clubs = clubService.getClubsByDepartment(department);
        ApiResponse<List<ClubByDepartmentResponse>> response = new ApiResponse<>("분과별 동아리 조회 성공", clubs);
        return ResponseEntity.ok(response);
    }

    //해당 동아리 지원서 조회
    @GetMapping("/aplict/{clubId}")
    public ResponseEntity<ApiResponse<List<AplictResponse>>> getAplictByClubId(@PathVariable("clubId") Long clubId) {
        List<AplictResponse> aplicts = clubService.getAplictByClubId(clubId);
        ApiResponse<List<AplictResponse>> response = new ApiResponse<>("지원서 조회 성공", aplicts);
        return ResponseEntity.ok(response);
    }

}