package com.USWCicrcleLink.server.club.club.api;
import com.USWCicrcleLink.server.club.club.dto.ClubCategoryResponse;
import com.USWCicrcleLink.server.club.club.service.ClubService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.user.dto.ClubInfoListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.USWCicrcleLink.server.club.club.dto.ClubByRecruitmentStatusFilterResponse;
import com.USWCicrcleLink.server.club.club.dto.ClubFilterResponse;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs")
public class ClubController {

    private final ClubService clubService;
    @GetMapping("/filter/{categories}")
    public ResponseEntity<ApiResponse<List<ClubFilterResponse>>> getFilteringClubs(
            @PathVariable("categories") List<String> categories){
        List<ClubFilterResponse> clubFilterResponse = clubService.getClubsByCategories(categories);
        ApiResponse<List<ClubFilterResponse>> response = new ApiResponse<>("카테고리별 전체 동아리 조회 완료",clubFilterResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter/{categories}/open")
    public ResponseEntity<ApiResponse<List<ClubByRecruitmentStatusFilterResponse>>> getFilteringClubsByRecruitmentStatus(
            @PathVariable("categories") List<String> categories){
        List<ClubByRecruitmentStatusFilterResponse> clubByRecruitmentStatusFilterResponses = clubService.getClubsByRecruitmentStatusCategories(categories);
        ApiResponse<List<ClubByRecruitmentStatusFilterResponse>> response = new ApiResponse<>("카테고리별 모집중인 동아리 조회 완료", clubByRecruitmentStatusFilterResponses);
        return ResponseEntity.ok(response);
    }

    //카테고리 조회
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<ClubCategoryResponse>>> getAllCategories() {
        List<ClubCategoryResponse> clubCategoryResponses = clubService.getAllCategories();
        ApiResponse<List<ClubCategoryResponse>> response = new ApiResponse<>("카테고리 조회 완료", clubCategoryResponses);
        return ResponseEntity.ok(response);
    }

    // 모바일 기존회원가입시 모든 동아리 출력
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<ClubInfoListResponse>>> getAllClubs() {
        List<ClubInfoListResponse> clubs = clubService.getAllClubs();
        ApiResponse<List<ClubInfoListResponse>> response = new ApiResponse<>("동아리 리스트 조회 성공", clubs);
        return ResponseEntity.ok(response);
    }
}
