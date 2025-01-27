package com.USWCicrcleLink.server.admin.admin.api;

import com.USWCicrcleLink.server.admin.admin.dto.*;
import com.USWCicrcleLink.server.admin.admin.service.AdminClubService;
import com.USWCicrcleLink.server.club.clubIntro.dto.ClubIntroResponse;
import com.USWCicrcleLink.server.club.clubIntro.service.ClubIntroService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/clubs")
@RequiredArgsConstructor
public class AdminClubController {

    private final AdminClubService adminClubService;
    private final ClubIntroService clubIntroService;

    // 동아리 리스트 조회(웹, 페이징)
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminClubListResponse>>> getAllClubs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("clubId").descending());
        Page<AdminClubListResponse> pagedClubs = adminClubService.getAllClubs(pageable);
        ApiResponse<Page<AdminClubListResponse>> response = new ApiResponse<>("동아리 전체 리스트 조회 성공", pagedClubs);
        return ResponseEntity.ok(response);
    }

    // 동아리 상세 페이지 조회(웹)
    @GetMapping("/{clubId}")
    public ResponseEntity<ApiResponse<ClubIntroResponse>> getClubById(@PathVariable("clubId") Long clubId) {
        ClubIntroResponse clubIntroResponse = clubIntroService.getClubIntro(clubId);
        ApiResponse<ClubIntroResponse> response = new ApiResponse<>("동아리 상세 조회 성공", clubIntroResponse);
        return ResponseEntity.ok(response);
    }

    // 동아리 생성(웹) - 동아리 생성 완료하기
    @PostMapping()
    public ResponseEntity<ApiResponse<String>> createClub(@RequestBody @Valid ClubCreationRequest clubRequest) {
        adminClubService.createClub(clubRequest); // 동아리 생성
        ApiResponse<String> response = new ApiResponse<>("동아리 생성 성공");
        return ResponseEntity.ok(response);
    }

    // 동아리 삭제(웹)
    @DeleteMapping("{clubId}")
    public ResponseEntity<ApiResponse<Long>> deleteClub(@PathVariable("clubId") Long clubId, @RequestBody @Valid AdminPwRequest request) {
        adminClubService.deleteClub(clubId, request);
        ApiResponse<Long> response = new ApiResponse<>("동아리 삭제 성공");
        return ResponseEntity.ok(response);
    }

    // 동아리 생성(웹) - 동아리 회장 아이디 중복 확인
    @GetMapping("/leader/check")
    public ResponseEntity<ApiResponse<String>> checkLeaderAccountDuplicate(
            @RequestParam("leaderAccount") String leaderAccount) {
        // 중복 확인 - 중복된 경우 예외 발생
        adminClubService.validateLeaderAccount(leaderAccount);

        // 성공 응답 반환
        ApiResponse<String> response = new ApiResponse<>("사용 가능한 동아리 회장 아이디입니다.");
        return ResponseEntity.ok(response);
    }

    // 동아리 생성(웹) - 동아리 이름 중복 확인
    @GetMapping("/name/check")
    public ResponseEntity<ApiResponse<String>> checkClubNameDuplicate(
            @RequestParam("clubName") String clubName) {
        // 중복 확인 - 중복된 경우 예외 발생
        adminClubService.validateClubName(clubName);

        // 성공 응답 반환
        ApiResponse<String> response = new ApiResponse<>("사용 가능한 동아리 이름입니다.");
        return ResponseEntity.ok(response);
    }
}