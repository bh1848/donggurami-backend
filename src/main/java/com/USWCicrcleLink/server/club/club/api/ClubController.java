package com.USWCicrcleLink.server.club.club.api;

import com.USWCicrcleLink.server.club.club.dto.ClubListResponse;
import com.USWCicrcleLink.server.club.club.service.ClubService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.user.dto.ClubInfoListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clubs")
public class ClubController {

    private final ClubService clubService;

    // 모든 동아리 출력
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<ClubInfoListResponse>>> getAllClubs() {
        List<ClubInfoListResponse> clubs = clubService.getAllClubs();
        ApiResponse<List<ClubInfoListResponse>> response = new ApiResponse<>("동아리 리스트 조회 성공", clubs);
        return ResponseEntity.ok(response);
    }


}
