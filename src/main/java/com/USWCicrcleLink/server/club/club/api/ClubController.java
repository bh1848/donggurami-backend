package com.USWCicrcleLink.server.club.club.api;

import com.USWCicrcleLink.server.club.club.dto.ClubByRecruitmentStatusFilterResponse;
import com.USWCicrcleLink.server.club.club.dto.ClubFilterResponse;
import com.USWCicrcleLink.server.club.club.service.ClubService;
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
}
