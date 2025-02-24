package com.USWCicrcleLink.server.clubLeader.api;

import com.USWCicrcleLink.server.clubLeader.dto.LeaderLoginRequest;
import com.USWCicrcleLink.server.clubLeader.dto.LeaderLoginResponse;
import com.USWCicrcleLink.server.clubLeader.service.ClubLeaderAuthService;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/club-leader")
@Slf4j
public class ClubLeaderAuthController {

    private final ClubLeaderAuthService clubLeaderAuthService;

    /**
     * 로그인 (Leader)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LeaderLoginResponse>> LeaderLogin(@RequestBody LeaderLoginRequest request, HttpServletResponse response){
        LeaderLoginResponse leaderLoginResponse = clubLeaderAuthService.leaderLogin(request,response);
        ApiResponse<LeaderLoginResponse> apiResponse = new ApiResponse<>("동아리 회장 로그인 성공", leaderLoginResponse);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * 로그아웃 (Leader)
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        clubLeaderAuthService.leaderLogout(request, response);
        return ResponseEntity.ok().build();
    }
}