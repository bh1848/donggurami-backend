package com.USWCicrcleLink.server.admin.admin.api;

import com.USWCicrcleLink.server.admin.admin.dto.*;
import com.USWCicrcleLink.server.admin.admin.service.AdminService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.security.dto.TokenDto;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<ApiResponse<TokenDto>> loginAdmin(HttpServletRequest request, @RequestBody AdminLoginRequest loginRequest) {
        TokenDto tokenDto = adminService.adminLogin(request, loginRequest);
        ApiResponse<TokenDto> response = new ApiResponse<>("로그인 성공", tokenDto);
        return ResponseEntity.ok(response);
    }

    // 동아리 전체 리스트 조회(웹)
    @GetMapping("/clubs")
    public ResponseEntity<ApiResponse<List<ClubListResponse>>> getAllClubs(HttpServletRequest request) {
        List<ClubListResponse> clubs = adminService.getAllClubs(request);
        ApiResponse<List<ClubListResponse>> response = new ApiResponse<>("동아리 전체 리스트 조회 성공", clubs);
        return ResponseEntity.ok(response);
    }

    // 동아리 상세 페이지 조회(웹)
    @GetMapping("/clubs/{clubId}")
    public ResponseEntity<ApiResponse<ClubDetailResponse>> getClubById(HttpServletRequest request, @PathVariable("clubId") Long clubId) {
        ClubDetailResponse clubDetailResponse = adminService.getClubById(request, clubId);
        ApiResponse<ClubDetailResponse> response = new ApiResponse<>("동아리 상세 조회 성공", clubDetailResponse);
        return ResponseEntity.ok(response);
    }

    // 동아리 생성(웹)
    @PostMapping("/clubs")
    public ResponseEntity<ApiResponse<Void>> createClub(HttpServletRequest request, @RequestBody ClubCreationRequest clubRequest) {
        adminService.createClub(request, clubRequest);
        ApiResponse<Void> response = new ApiResponse<>("동아리 생성 성공");
        return ResponseEntity.ok(response);
    }

    // 동아리 삭제(웹)
    @DeleteMapping("/clubs/{clubId}")
    public ResponseEntity<ApiResponse<Long>> deleteClub(HttpServletRequest request, @PathVariable("clubId") Long clubId, @RequestBody AdminPwRequest pwRequest) {
        adminService.deleteClub(request, clubId, pwRequest.getAdminPw());
        ApiResponse<Long> response = new ApiResponse<>("동아리 삭제 성공", clubId);
        return ResponseEntity.ok(response);
    }
}
