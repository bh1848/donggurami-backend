package com.USWCicrcleLink.server.admin.admin.api;

import com.USWCicrcleLink.server.admin.admin.dto.*;
import com.USWCicrcleLink.server.admin.admin.service.AdminService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.security.dto.TokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    // 관리자 로그인(웹)
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenDto>> loginAdmin(@RequestBody AdminLoginRequest loginRequest) {
        TokenDto tokenDto = adminService.adminLogin(loginRequest);
        ApiResponse<TokenDto> response = new ApiResponse<>("로그인 성공", tokenDto);
        return ResponseEntity.ok(response);
    }

    // 동아리 전체 리스트 조회(웹)
    @GetMapping("/clubs")
    public ResponseEntity<ApiResponse<List<ClubListResponse>>> getAllClubs() {
        List<ClubListResponse> clubs = adminService.getAllClubs();
        ApiResponse<List<ClubListResponse>> response = new ApiResponse<>("동아리 전체 리스트 조회 성공", clubs);
        return ResponseEntity.ok(response);
    }

    // 동아리 상세 페이지 조회(웹)
    @GetMapping("/clubs/{clubId}")
    public ResponseEntity<ApiResponse<ClubDetailResponse>> getClubById(@PathVariable("clubId") Long clubId) {
        ClubDetailResponse clubDetailResponse = adminService.getClubById(clubId);
        ApiResponse<ClubDetailResponse> response = new ApiResponse<>("동아리 상세 조회 성공", clubDetailResponse);
        return ResponseEntity.ok(response);
    }

    // 동아리 생성(웹)
    @PostMapping("/clubs")
    public ResponseEntity<ApiResponse<ClubCreationResponse>> createClub(@RequestBody ClubCreationRequest clubRequest) {
        ClubCreationResponse clubCreationResponse = adminService.createClub(clubRequest);
        ApiResponse<ClubCreationResponse> response = new ApiResponse<>("동아리 생성 성공", clubCreationResponse);
        return ResponseEntity.ok(response);
    }

    // 동아리 삭제(웹)
    @DeleteMapping("/clubs/{clubId}")
    public ResponseEntity<ApiResponse<Long>> deleteClub(@PathVariable("clubId") Long clubId, @RequestBody AdminPwRequest pwRequest) {
        adminService.deleteClub(clubId, pwRequest.getAdminPw());
        ApiResponse<Long> response = new ApiResponse<>("동아리 삭제 성공: clubId = {}", clubId);
        return ResponseEntity.ok(response);
    }
}
