package com.USWCicrcleLink.server.club.club.api;

import com.USWCicrcleLink.server.club.club.dto.ClubCategoryResponse;
import com.USWCicrcleLink.server.club.club.dto.ClubListByClubCategoryResponse;
import com.USWCicrcleLink.server.club.club.dto.ClubListResponse;
import com.USWCicrcleLink.server.club.club.service.ClubService;
import com.USWCicrcleLink.server.club.clubIntro.dto.ClubIntroResponse;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs")
public class ClubController {

    private final ClubService clubService;

    // 전체 동아리 조회 (모바일)
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClubListResponse>>> getAllClubs() {
        List<ClubListResponse> clubs = clubService.getAllClubs();
        return ResponseEntity.ok(new ApiResponse<>("전체 동아리 조회 완료", clubs));
    }

    // 카테고리별 전체 동아리 조회 (모바일)
    @GetMapping("filter")
    public ResponseEntity<ApiResponse<List<ClubListByClubCategoryResponse>>> getAllClubsByClubCategories(@RequestParam(defaultValue = "") List<UUID> clubCategoryUUIDs) {
        List<ClubListByClubCategoryResponse> clubs = clubService.getAllClubsByClubCategories(clubCategoryUUIDs);
        return ResponseEntity.ok(new ApiResponse<>("카테고리별 전체 동아리 조회 완료", clubs));
    }

    // 모집 중인 전체 동아리 조회
    @GetMapping("/open")
    public ResponseEntity<ApiResponse<List<ClubListResponse>>> getOpenClubs() {
        List<ClubListResponse> clubs = clubService.getOpenClubs();
        return ResponseEntity.ok(new ApiResponse<>("모집 중인 동아리 조회 완료", clubs));
    }

    // 카테고리별 모집 중인 동아리 조회
    @GetMapping("/open/filter")
    public ResponseEntity<ApiResponse<List<ClubListByClubCategoryResponse>>> getOpenClubsByCategories(
            @RequestParam(defaultValue = "") List<UUID> clubCategoryUUIDs) {
        List<ClubListByClubCategoryResponse> clubs = clubService.getOpenClubsByClubCategories(clubCategoryUUIDs);
        return ResponseEntity.ok(new ApiResponse<>("카테고리별 모집 중인 동아리 조회 완료", clubs));
    }

    // 카테고리 리스트 조회 (모바일)
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<ClubCategoryResponse>>> getAllClubCategories() {
        List<ClubCategoryResponse> clubCategoryResponses = clubService.getAllClubCategories();
        return ResponseEntity.ok(new ApiResponse<>("카테고리 조회 완료", clubCategoryResponses));
    }

    // 동아리 소개글 조회 (모바일)
    @GetMapping("/intro/{clubId}")
    public ResponseEntity<ApiResponse<ClubIntroResponse>> getClubIntroByClubId(@PathVariable("clubId") Long id) {
        ClubIntroResponse clubIntroResponse = clubService.getClubIntro(id);
        return ResponseEntity.ok(new ApiResponse<>("동아리 소개글 조회 성공", clubIntroResponse));
    }
}
