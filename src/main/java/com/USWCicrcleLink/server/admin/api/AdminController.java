package com.USWCicrcleLink.server.admin.api;

import com.USWCicrcleLink.server.admin.dto.AdminPwRequest;
import com.USWCicrcleLink.server.admin.dto.ClubCreationRequest;
import com.USWCicrcleLink.server.admin.dto.ClubDetailResponse;
import com.USWCicrcleLink.server.admin.dto.ClubListResponse;
import com.USWCicrcleLink.server.admin.service.AdminService;
import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    //동아리 전체 리스트 조회
    @GetMapping("/clubs")
    public ResponseEntity<ApiResponse<List<ClubListResponse>>> getAllClubs() {
        List<ClubListResponse> clubs = adminService.getAllClubs();
        ApiResponse<List<ClubListResponse>> response = new ApiResponse<>("동아리 전체 리스트 조회 성공", clubs);
        return ResponseEntity.ok(response);
    }

    //동아리 상세 페이지 조회
    @GetMapping("/clubs/{clubId}")
    public ResponseEntity<ApiResponse<ClubDetailResponse>> getClubById(@PathVariable("clubId") Long clubId) {
        ClubDetailResponse clubDetailResponse = adminService.getClubById(clubId);
        ApiResponse<ClubDetailResponse> response = new ApiResponse<>("동아리 상세 조회 성공", clubDetailResponse);
        return ResponseEntity.ok(response);
    }

    //동아리 생성
    @PostMapping("/club/create")
    public ResponseEntity<ApiResponse<Club>> createClub(@RequestBody ClubCreationRequest request) {
        adminService.createClub(request);
        ApiResponse<Club> response = new ApiResponse<>("동아리 생성 성공");
        return ResponseEntity.ok(response);
    }

    //동아리 삭제
    @DeleteMapping("/club/delete/{clubId}")
    public ResponseEntity<ApiResponse<Long>> deleteClub(@PathVariable("clubId") Long clubId, @RequestBody AdminPwRequest request) {
        adminService.deleteClub(clubId, request.getAdminPw());
        ApiResponse<Long> response = new ApiResponse<>("동아리 삭제 성공", clubId);
        return ResponseEntity.ok(response);
    }
}
