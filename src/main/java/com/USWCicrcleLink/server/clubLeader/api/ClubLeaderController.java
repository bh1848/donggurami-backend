package com.USWCicrcleLink.server.clubLeader.api;

import com.USWCicrcleLink.server.aplict.dto.ApplicantResultsRequest;
import com.USWCicrcleLink.server.aplict.dto.ApplicantsResponse;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.clubLeader.dto.*;
import com.USWCicrcleLink.server.clubLeader.service.ClubLeaderService;
import com.USWCicrcleLink.server.clubLeader.service.FcmServiceImpl;
import com.USWCicrcleLink.server.global.response.ApiResponse;
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

    @GetMapping("/info")
    public ResponseEntity<ApiResponse> getClubInfo(LeaderToken token) {
        ApiResponse<ClubInfoResponse> clubInfo = clubLeaderService.getClubInfo(token);
        return new ResponseEntity<>(clubInfo, HttpStatus.OK);
    }

    @PatchMapping("/info")
    public ResponseEntity<Boolean> updateClubInfo(LeaderToken token, @Validated ClubInfoRequest clubInfoRequest) throws IOException {
        clubLeaderService.updateClubInfo(token, clubInfoRequest);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @PatchMapping("/intro")
    public ResponseEntity<Boolean> setClubInfo(LeaderToken token, @Validated ClubIntroRequest clubInfoRequest) throws IOException {
        clubLeaderService.updateClubIntro(token, clubInfoRequest);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @PatchMapping("/toggle-recruitment")
    public ResponseEntity<RecruitmentStatus> setClubInfo(LeaderToken token) {
        // 원래는 Patch 요청임 토큰때문
        return new ResponseEntity<>(clubLeaderService.toggleRecruitmentStatus(token), HttpStatus.OK);
    }

    @GetMapping("/members")
    public ResponseEntity<ApiResponse> getClubMembers(LeaderToken token) {
        // 원래는 GET 요청임 토큰때문
        return new ResponseEntity<>(clubLeaderService.findClubMembers(token), HttpStatus.OK);
    }

    @DeleteMapping("/members/{clubMemberId}")
    public ResponseEntity<ApiResponse> deleteClubMember(@PathVariable Long clubMemberId, LeaderToken token) {
        return new ResponseEntity<>(clubLeaderService.deleteClubMember(clubMemberId, token), HttpStatus.OK);
    }

    @GetMapping("/members/export")
    public void exportClubMembers(LeaderToken token, HttpServletResponse response) {
        // 엑셀 파일 생성
        clubLeaderService.downloadExcel(token, response);
    }

    @GetMapping("/applicants")
    public Page<ApplicantsResponse> getApplicants(@RequestBody LeaderToken token, @RequestParam int page, @RequestParam int size) {
        return clubLeaderService.getApplicants(token, page, size);
    }

    @PostMapping("/applicants/notifyMultiple")
    public ResponseEntity<ApiResponse> pushApplicantResults(LeaderToken token, List<ApplicantResultsRequest> results) {
        clubLeaderService.updateApplicantResults(token, results);
        return new ResponseEntity<>(new ApiResponse<>("지원 결과 처리 완료: {}"), HttpStatus.OK);
    }

    @GetMapping("/failed-applicants")
    public Page<ApplicantsResponse> getFailedApplicants(@RequestBody LeaderToken token, @RequestParam int page, @RequestParam int size) {
        return clubLeaderService.getFailedApplicants(token, page, size);
    }

    @PostMapping("/failed-applicants/notifyMultiple")
    public ResponseEntity<ApiResponse> pushFailedApplicantResults(LeaderToken token, List<ApplicantResultsRequest> results) {
        clubLeaderService.updateFailedApplicantResults(token, results);
        return new ResponseEntity<>(new ApiResponse<>("추합 결과 처리 완료: {}"), HttpStatus.OK);
    }

    @PostMapping("/applicants/notify")
    public ResponseEntity<Integer> pushMessage(@RequestBody @Validated FcmSendDto fcmSendDto) throws IOException {
        int result = fcmService.sendMessageTo(fcmSendDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
