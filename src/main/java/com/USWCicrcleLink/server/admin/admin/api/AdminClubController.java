package com.USWCicrcleLink.server.admin.admin.api;

import com.USWCicrcleLink.server.admin.admin.dto.*;
import com.USWCicrcleLink.server.admin.admin.service.AdminClubService;
import com.USWCicrcleLink.server.club.club.service.ClubService;
import com.USWCicrcleLink.server.admin.admin.dto.AdminClubIntroResponse;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/clubs")
@RequiredArgsConstructor
public class AdminClubController {

    private final AdminClubService adminClubService;
    private final ClubService clubService;

    // 메인 페이지(웹) - 동아리 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<AdminClubPageListResponse>> getAllClubs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("clubId").descending());
        AdminClubPageListResponse pagedClubs = adminClubService.getAllClubs(pageable);
        return ResponseEntity.ok(new ApiResponse<>("동아리 리스트 조회 성공", pagedClubs));
    }

    // 동아리 소개/모집글 페이지 조회 (웹 - 운영팀)
    @GetMapping("/{clubUUID}")
    public ResponseEntity<ApiResponse<AdminClubIntroResponse>> getClubById(@PathVariable("clubUUID") UUID clubUUID) {
        AdminClubIntroResponse clubIntroResponse = clubService.getClubIntro(clubUUID);
        return ResponseEntity.ok(new ApiResponse<>("동아리 소개/모집글 페이지 조회 성공", clubIntroResponse));
    }

    // 동아리 추가 (웹) - 동아리 추가
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createClub(@RequestBody @Valid AdminClubCreationRequest clubRequest) {
        adminClubService.createClub(clubRequest);
        return ResponseEntity.ok(new ApiResponse<>("동아리 생성 성공"));
    }

    // 동아리 삭제 (웹)
    @DeleteMapping("{clubUUID}")
    public ResponseEntity<ApiResponse<Long>> deleteClub(@PathVariable("clubUUID") UUID clubUUID, @RequestBody @Valid AdminPwRequest request) {
        adminClubService.deleteClub(clubUUID, request);
        return ResponseEntity.ok(new ApiResponse<>("동아리 삭제 성공"));
    }

    // 동아리 추가 (웹) - 동아리 회장 아이디 중복 확인
    @GetMapping("/leader/check")
    public ResponseEntity<ApiResponse<String>> checkLeaderAccountDuplicate(
            @RequestParam("leaderAccount") String leaderAccount) {
        adminClubService.validateLeaderAccount(leaderAccount);
        return ResponseEntity.ok(new ApiResponse<>("사용 가능한 동아리 회장 아이디입니다."));
    }

    // 동아리 추가 (웹) - 동아리 이름 중복 확인
    @GetMapping("/name/check")
    public ResponseEntity<ApiResponse<String>> checkClubNameDuplicate(
            @RequestParam("clubName") String clubName) {
        adminClubService.validateClubName(clubName);
        return ResponseEntity.ok(new ApiResponse<>("사용 가능한 동아리 이름입니다."));
    }
}