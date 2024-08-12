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
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/club-leader")
public class ClubLeaderController {

    private final ClubLeaderService clubLeaderService;
    private final FcmServiceImpl fcmService;

    // 동아리 기본 정보 조회
    @GetMapping("/{clubId}/info")
    public ResponseEntity<ApiResponse> getClubInfo(@PathVariable("clubId") Long clubId) {
        ApiResponse<ClubInfoResponse> clubInfo = clubLeaderService.getClubInfo(clubId);
        return new ResponseEntity<>(clubInfo, HttpStatus.OK);
    }

    // 동아리 기본 정보 변경
    @PatchMapping("/{clubId}/info")
    public ResponseEntity<ApiResponse> updateClubInfo(@PathVariable("clubId") Long clubId,
                                                      @RequestPart(value = "mainPhoto", required = false) MultipartFile mainPhoto,
                                                      @RequestParam(value = "leaderName", required = false) String leaderName,
                                                      @RequestParam(value = "leaderHp", required = false) String leaderHp,
                                                      @RequestParam(value = "clubInsta", required = false) String clubInsta) throws IOException {

        ClubInfoRequest clubInfoRequest = new ClubInfoRequest();
        clubInfoRequest.setMainPhoto(mainPhoto);
        clubInfoRequest.setLeaderName(leaderName);
        clubInfoRequest.setLeaderHp(leaderHp);
        clubInfoRequest.setClubInsta(clubInsta);

        return new ResponseEntity<>(clubLeaderService.updateClubInfo(clubId, clubInfoRequest), HttpStatus.OK);
    }

    // 동아리 소개 조회
    @GetMapping("/{clubId}/intro")
    public ResponseEntity<ApiResponse> getClubIntro(@PathVariable("clubId") Long clubId) {
        ApiResponse<ClubIntroResponse> clubIntro = clubLeaderService.getClubIntro(clubId);
        return new ResponseEntity<>(clubIntro, HttpStatus.OK);
    }

    // 동아리 소개 변경
    @PatchMapping("/{clubId}/intro")
    public ResponseEntity<ApiResponse> updateClubIntro(@PathVariable("clubId") Long clubId,
                                                       @RequestPart(value = "clubIntro", required = false) String clubIntro,
                                                       @RequestPart(value = "googleFormUrl", required = false) String googleFormUrl,
                                                       @RequestPart(value = "introPhoto", required = false) MultipartFile introPhoto,
                                                       @RequestPart(value = "additionalPhoto1", required = false) MultipartFile additionalPhoto1,
                                                       @RequestPart(value = "additionalPhoto2", required = false) MultipartFile additionalPhoto2,
                                                       @RequestPart(value = "additionalPhoto3", required = false) MultipartFile additionalPhoto3,
                                                       @RequestPart(value = "additionalPhoto4", required = false) MultipartFile additionalPhoto4) throws IOException {

        ClubIntroRequest clubIntroRequest = new ClubIntroRequest();
        clubIntroRequest.setClubIntro(clubIntro);
        clubIntroRequest.setGoogleFormUrl(googleFormUrl);
        clubIntroRequest.setIntroPhoto(introPhoto);
        clubIntroRequest.setAdditionalPhoto1(additionalPhoto1);
        clubIntroRequest.setAdditionalPhoto2(additionalPhoto2);
        clubIntroRequest.setAdditionalPhoto3(additionalPhoto3);
        clubIntroRequest.setAdditionalPhoto4(additionalPhoto4);
        return new ResponseEntity<>(clubLeaderService.updateClubIntro(clubId, clubIntroRequest), HttpStatus.OK);
    }

    // 동아리 모집 상태 변경
    @PatchMapping("/{clubId}/toggle-recruitment")
    public ResponseEntity<ApiResponse> toggleRecruitmentStatus(@PathVariable("clubId") Long clubId) {
        return new ResponseEntity<>(clubLeaderService.toggleRecruitmentStatus(clubId), HttpStatus.OK);
    }

//    @GetMapping("/v1/members")
//    public ResponseEntity<ApiResponse> getClubMembers(LeaderToken token) {
//        // 원래는 GET 요청임 토큰때문
//        return new ResponseEntity<>(clubLeaderService.findClubMembers(token), HttpStatus.OK);
//    }

    // 소속 동아리원 조회
    @GetMapping("/{clubId}/members")
    public ResponseEntity<ApiResponse> getClubMembers(@PathVariable("clubId") Long clubId, @RequestParam("page") int page, @RequestParam("size") int size) {
        return new ResponseEntity<>(clubLeaderService.getClubMembers(clubId, page, size), HttpStatus.OK);
    }

    // 동아리원 퇴출
    @DeleteMapping("/{clubId}/members/{clubMemberId}")
    public ResponseEntity<ApiResponse> deleteClubMember(@PathVariable("clubMemberId") Long clubMemberId, @PathVariable("clubId") Long clubId) {
        return new ResponseEntity<>(clubLeaderService.deleteClubMember(clubMemberId, clubId), HttpStatus.OK);
    }

    // 동아리원 엑셀 파일 추출
    @GetMapping("/{clubId}/members/export")
    public void exportClubMembers(@PathVariable("clubId") Long clubId, HttpServletResponse response) {
        // 엑셀 파일 생성
        clubLeaderService.downloadExcel(clubId, response);
    }

    // 최초 지원자 조회
    @GetMapping("/{clubId}/applicants")
    public ResponseEntity<ApiResponse> getApplicants(@PathVariable("clubId") Long clubId, @RequestParam("page") int page, @RequestParam("size") int size) {
        return new ResponseEntity<>(clubLeaderService.getApplicants(clubId, page, size), HttpStatus.OK);
    }

    // 최초 합격자 알림
    @PostMapping("/{clubId}/applicants/notifyMultiple")
    public ResponseEntity<ApiResponse> pushApplicantResults(@PathVariable("clubId") Long clubId, @RequestBody List<ApplicantResultsRequest> results) throws IOException {
        clubLeaderService.updateApplicantResults(clubId, results);
        return new ResponseEntity<>(new ApiResponse<>("지원 결과 처리 완료"), HttpStatus.OK);
    }

    // 불합격자 조회
    @GetMapping("/{clubId}/failed-applicants")
    public ApiResponse<PageResponse> getFailedApplicants(@PathVariable("clubId") Long clubId, @RequestParam("page") int page, @RequestParam("size") int size) {
        return clubLeaderService.getFailedApplicants(clubId, page, size);
    }

    // 지원자 추가 합격 알림
    @PostMapping("/{clubId}/failed-applicants/notifyMultiple")
    public ResponseEntity<ApiResponse> pushFailedApplicantResults(@PathVariable("clubId") Long clubId, @RequestBody List<ApplicantResultsRequest> results) throws IOException {
        clubLeaderService.updateFailedApplicantResults(clubId, results);
        return new ResponseEntity<>(new ApiResponse<>("추합 결과 처리 완료"), HttpStatus.OK);
    }

    // fcm token 저장 테스트 실제로는 로그인에서 처리해야함
    @PostMapping("/fcm-token")
    public ResponseEntity<String> getFcmToken(FcmTokenTestRequest fcmTestRequest) {
        clubLeaderService.updateFcmToken(fcmTestRequest);
        return new ResponseEntity<>("fcm token: " + fcmTestRequest.getFcmToken(), HttpStatus.OK);
    }
}
