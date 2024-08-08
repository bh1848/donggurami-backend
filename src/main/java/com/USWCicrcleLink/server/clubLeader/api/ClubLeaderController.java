package com.USWCicrcleLink.server.clubLeader.api;

import com.USWCicrcleLink.server.aplict.dto.ApplicantResultsRequest;
import com.USWCicrcleLink.server.aplict.dto.ApplicantsResponse;
import com.USWCicrcleLink.server.clubLeader.dto.*;
import com.USWCicrcleLink.server.clubLeader.service.ClubLeaderService;
import com.USWCicrcleLink.server.clubLeader.service.FcmServiceImpl;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.response.PageResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/club-leader")
public class ClubLeaderController {

    private final ClubLeaderService clubLeaderService;
    private final FcmServiceImpl fcmService;

    // 동아리 기본 정보 조회
    @GetMapping("/info")
    public ResponseEntity<ApiResponse> getClubInfo(@RequestParam("leaderUUID") String leaderUUID) {
        ApiResponse<ClubInfoResponse> clubInfo = clubLeaderService.getClubInfo(leaderUUID);
        return new ResponseEntity<>(clubInfo, HttpStatus.OK);
    }

    // 동아리 기본 정보 변경
    @PatchMapping("/info")
    public ResponseEntity<ApiResponse> updateClubInfo(@RequestParam("leaderUUID") String leaderUUID, @Validated ClubInfoRequest clubInfoRequest) throws IOException {
        return new ResponseEntity<>(clubLeaderService.updateClubInfo(leaderUUID, clubInfoRequest), HttpStatus.OK);
    }

    // 동아리 소개 변경
    @PatchMapping("/intro")
    public ResponseEntity<ApiResponse> updateClubIntro(@RequestParam("leaderUUID") String leaderUUID, @Validated ClubIntroRequest clubInfoRequest) throws IOException {
        return new ResponseEntity<>(clubLeaderService.updateClubIntro(leaderUUID, clubInfoRequest), HttpStatus.OK);
    }

    // 동아리 모집 상태 변경
    @PatchMapping("/toggle-recruitment")
    public ResponseEntity<ApiResponse> toggleRecruitmentStatus(@RequestParam("leaderUUID") String leaderUUID) {
        return new ResponseEntity<>(clubLeaderService.toggleRecruitmentStatus(leaderUUID), HttpStatus.OK);
    }

//    @GetMapping("/v1/members")
//    public ResponseEntity<ApiResponse> getClubMembers(LeaderToken token) {
//        // 원래는 GET 요청임 토큰때문
//        return new ResponseEntity<>(clubLeaderService.findClubMembers(token), HttpStatus.OK);
//    }

    // 소속 동아리원 조회
    @GetMapping("/members")
    public ResponseEntity<ApiResponse> getClubMembers(@RequestParam("leaderUUID") String leaderUUID, @RequestParam("page") int page, @RequestParam("size") int size) {
        // 원래는 GET 요청임 토큰때문
        return new ResponseEntity<>(clubLeaderService.getClubMembers(leaderUUID, page, size), HttpStatus.OK);
    }

    // 동아리원 퇴출
    @DeleteMapping("/members/{clubMemberId}")
    public ResponseEntity<ApiResponse> deleteClubMember(@PathVariable("clubMemberId") Long clubMemberId, @RequestParam("leaderUUID") String leaderUUID) {
        return new ResponseEntity<>(clubLeaderService.deleteClubMember(clubMemberId, leaderUUID), HttpStatus.OK);
    }

    // 동아리원 엑셀 파일 추출
    @GetMapping("/members/export")
    public void exportClubMembers(@RequestParam("leaderUUID") String leaderUUID, HttpServletResponse response) {
        // 엑셀 파일 생성
        clubLeaderService.downloadExcel(leaderUUID, response);
    }

    // 최초 지원자 조회
    @GetMapping("/applicants")
    public ResponseEntity<ApiResponse> getApplicants(@RequestParam("leaderUUID") String leaderUUID, @RequestParam("page") int page, @RequestParam("size") int size) {
        return new ResponseEntity<>(clubLeaderService.getApplicants(leaderUUID, page, size), HttpStatus.OK);
    }

    // 최초 합격자 알림
    @PostMapping("/applicants/notifyMultiple")
    public ResponseEntity<ApiResponse> pushApplicantResults(@RequestParam("leaderUUID") String leaderUUID, List<ApplicantResultsRequest> results) throws IOException {
        clubLeaderService.updateApplicantResults(leaderUUID, results);
        return new ResponseEntity<>(new ApiResponse<>("지원 결과 처리 완료"), HttpStatus.OK);
    }

    // 불합격자 조회
    @GetMapping("/failed-applicants")
    public ApiResponse<PageResponse> getFailedApplicants(@RequestParam("leaderUUID") String leaderUUID, @RequestParam("page") int page, @RequestParam("size") int size) {
        return clubLeaderService.getFailedApplicants(leaderUUID, page, size);
    }

    // 지원자 추가 합격 알림
    @PostMapping("/failed-applicants/notifyMultiple")
    public ResponseEntity<ApiResponse> pushFailedApplicantResults(@RequestParam("leaderUUID") String leaderUUID, List<ApplicantResultsRequest> results) throws IOException {
        clubLeaderService.updateFailedApplicantResults(leaderUUID, results);
        return new ResponseEntity<>(new ApiResponse<>("추합 결과 처리 완료"), HttpStatus.OK);
    }

    // fcm token 저장 테스트 실제로는 로그인에서 처리해야함
    @PostMapping("/fcm-token")
    public ResponseEntity<String> getFcmToken(FcmTokenTestRequest fcmTestRequest) {
        clubLeaderService.updateFcmToken(fcmTestRequest);
        return new ResponseEntity<>("fcm token: " + fcmTestRequest.getFcmToken(), HttpStatus.OK);
    }
}
