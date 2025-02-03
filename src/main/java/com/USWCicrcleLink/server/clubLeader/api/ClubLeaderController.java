package com.USWCicrcleLink.server.clubLeader.api;

import com.USWCicrcleLink.server.admin.admin.service.AdminClubCategoryService;
import com.USWCicrcleLink.server.aplict.dto.ApplicantResultsRequest;
import com.USWCicrcleLink.server.club.club.domain.ClubCategory;
import com.USWCicrcleLink.server.clubLeader.dto.*;
import com.USWCicrcleLink.server.clubLeader.service.ClubLeaderService;
import com.USWCicrcleLink.server.clubLeader.service.FcmServiceImpl;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.response.PageResponse;
import com.USWCicrcleLink.server.profile.domain.MemberType;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/club-leader")
@Slf4j
public class ClubLeaderController {

    private final ClubLeaderService clubLeaderService;
    private final AdminClubCategoryService adminClubCategoryService;
    private final FcmServiceImpl fcmService;

    // 동아리 기본 정보 조회
    @GetMapping("/{clubId}/info")
    public ResponseEntity<ApiResponse> getClubInfo(@PathVariable("clubId") Long clubId) {
        ApiResponse<ClubInfoResponse> clubInfo = clubLeaderService.getClubInfo(clubId);
        return new ResponseEntity<>(clubInfo, HttpStatus.OK);
    }

    // 동아리 기본 정보 변경 - 카테고리 조회
    @GetMapping("/category")
    public ResponseEntity<ApiResponse<List<ClubCategory>>> getAllCategories() {
        List<ClubCategory> categories = adminClubCategoryService.getAllCategories();
        ApiResponse<List<ClubCategory>> response = new ApiResponse<>("카테고리 리스트 조회 성공", categories);
        return ResponseEntity.ok(response);
    }

    // 동아리 기본 정보 변경
    @PutMapping("/{clubId}/info")
    public ResponseEntity<ApiResponse> updateClubInfo(@PathVariable("clubId") Long clubId,
                                                      @RequestPart(value = "mainPhoto", required = true) MultipartFile mainPhoto,
                                                      @Valid @RequestPart(value = "clubInfoRequest", required = false) ClubInfoRequest clubInfoRequest) throws IOException {

        return new ResponseEntity<>(clubLeaderService.updateClubInfo(clubId, clubInfoRequest, mainPhoto), HttpStatus.OK);
    }

    // 자신의 동아리 상세 페이지 조회(웹)
    @GetMapping("/{clubId}/intro")
    public ResponseEntity<ApiResponse<ClubIntroWebResponse>> getClubIntro(@PathVariable("clubId") Long clubId) {
        ClubIntroWebResponse clubIntroWebResponse = clubLeaderService.getClubIntro(clubId);
        ApiResponse<ClubIntroWebResponse> response = new ApiResponse<>("동아리 상세 조회 성공", clubIntroWebResponse);
        return ResponseEntity.ok(response);
    }

    // 동아리 소개 변경
    @PutMapping("/{clubId}/intro")
    public ResponseEntity<ApiResponse> updateClubIntro(@PathVariable("clubId") Long clubId,
                                                       @RequestPart(value = "clubIntroRequest", required = false) @Valid ClubIntroRequest clubIntroRequest,
                                                       @RequestPart(value = "introPhotos", required = false) List<MultipartFile> introPhotos) throws IOException {

        return new ResponseEntity<>(clubLeaderService.updateClubIntro(clubId, clubIntroRequest, introPhotos), HttpStatus.OK);
    }

    // 동아리 모집 상태 변경
    @PatchMapping("/{clubId}/recruitment")
    public ResponseEntity<ApiResponse> toggleRecruitmentStatus(@PathVariable("clubId") Long clubId) {
        return new ResponseEntity<>(clubLeaderService.toggleRecruitmentStatus(clubId), HttpStatus.OK);
    }

//    @GetMapping("/v1/members")
//    public ResponseEntity<ApiResponse> getClubMembers(LeaderToken token) {
//        // 원래는 GET 요청임 토큰때문
//        return new ResponseEntity<>(clubLeaderService.findClubMembers(token), HttpStatus.OK);
//    }

    // 소속 동아리 회원 조회
    @GetMapping("/{clubId}/members")
    public ResponseEntity<ApiResponse> getClubMembers(
            @PathVariable("clubId") Long clubId,
            @RequestParam(value = "sort", defaultValue = "default") String sort) {
        ApiResponse<List<ClubMembersResponse>> response;
        switch (sort) {
            case "regular-member":
                response = clubLeaderService.getClubMembersByMemberType(clubId, MemberType.REGULARMEMBER);
                break;

            case "non-member":
                response = clubLeaderService.getClubMembersByMemberType(clubId, MemberType.NONMEMBER);
                break;

            default:
                response = clubLeaderService.getClubMembers(clubId);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 동아리원 퇴출
    @DeleteMapping("/{clubId}/members")
    public ResponseEntity<ApiResponse> deleteClubMembers(@PathVariable("clubId") Long clubId, @RequestBody List<ClubMembersDeleteRequest> clubMemberIdList) {
        return new ResponseEntity<>(clubLeaderService.deleteClubMembers(clubId, clubMemberIdList), HttpStatus.OK);
    }

    // 동아리원 엑셀 파일 추출
    @GetMapping("/{clubId}/members/export")
    public ResponseEntity<ApiResponse> exportClubMembers(@PathVariable("clubId") Long clubId, HttpServletResponse response) {
        // 엑셀 파일 생성
        clubLeaderService.downloadExcel(clubId, response);
        return new ResponseEntity<>(new ApiResponse<>("동아리 회원 엑셀 파일 내보내기 완료"), HttpStatus.OK);
    }

    // fcm 토큰 갱신
    @PatchMapping("/fcmtoken")
    public ResponseEntity<ApiResponse> updateFcmToken(@RequestBody FcmTokenRequest fcmTokenRequest) {
        fcmService.refreshFcmToken(fcmTokenRequest);
        return new ResponseEntity<>(new ApiResponse<>("fcm token 갱신 완료"), HttpStatus.OK);
    }

    // 최초 지원자 조회
    @GetMapping("/{clubId}/applicants")
    public ResponseEntity<ApiResponse> getApplicants(@PathVariable("clubId") Long clubId, @RequestParam("page") int page, @RequestParam("size") int size) {
        return new ResponseEntity<>(clubLeaderService.getApplicants(clubId, page, size), HttpStatus.OK);
    }

    // 최초 합격자 알림
    @PostMapping("/{clubId}/applicants/notifications")
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
    @PostMapping("/{clubId}/failed-applicants/notifications")
    public ResponseEntity<ApiResponse> pushFailedApplicantResults(@PathVariable("clubId") Long clubId, @RequestBody List<ApplicantResultsRequest> results) throws IOException {
        clubLeaderService.updateFailedApplicantResults(clubId, results);
        return new ResponseEntity<>(new ApiResponse<>("추합 결과 처리 완료"), HttpStatus.OK);
    }

    // 기존 동아리원 엑셀 파일 업로드
    @PostMapping("/{clubId}/members/import")
    public ResponseEntity<ApiResponse<List<ClubMembersImportExcelResponse>>> importClubMembers(@PathVariable("clubId") Long clubId, @RequestPart(value = "clubMembersFile", required = false) MultipartFile clubMembersFile) throws IOException {
        return new ResponseEntity<>(clubLeaderService.uploadExcel(clubId, clubMembersFile), HttpStatus.OK);
    }

    // 기존 동아리원 엑셀 파일로 추가
    @PostMapping("/{clubId}/members")
    public ResponseEntity<ApiResponse> addClubMembersFromExcel(@PathVariable("clubId") Long clubId, @RequestBody List<ClubMembersAddFromExcelRequest> clubMembersAddFromExcelRequest) {
        clubLeaderService.addClubMembersFromExcel(clubId, clubMembersAddFromExcelRequest);
        return new ResponseEntity<>(new ApiResponse<>("엑셀로 추가된 기존 동아리 회원 저장 완료"), HttpStatus.OK);
    }

    // 프로필 중복 동아리 회원 추가
    @PostMapping("/{clubId}/members/duplicate-profiles")
    public ResponseEntity<ApiResponse> getDuplicateProfileMember(@PathVariable("clubId") Long clubId, @RequestBody DuplicateProfileMemberRequest duplicateProfileMemberRequest) {
        return new ResponseEntity<>(clubLeaderService.addDuplicateProfileMember(clubId, duplicateProfileMemberRequest), HttpStatus.OK);
    }

    // 비회원 프로필 업데이트
    @PatchMapping("/{clubId}/members/{clubMemberId}/non-member")
    public ResponseEntity<ApiResponse> updateNonMemberProfile(@PathVariable("clubId") Long clubId,
                                                              @PathVariable("clubMemberId") Long clubMemberId,
                                                              @RequestBody ClubNonMemberUpdateRequest clubNonMemberUpdateRequest) {
        return new ResponseEntity<>(clubLeaderService.updateNonMemberProfile(clubId, clubMemberId, clubNonMemberUpdateRequest), HttpStatus.OK);
    }

    // 기존 회원 가입 요청 조회
    @GetMapping("/{clubId}/members/sign-up")
    public ResponseEntity<ApiResponse> getSignUpRequest(@PathVariable("clubId") Long clubId) {
        return new ResponseEntity<>(clubLeaderService.getSignUpRequest(clubId), HttpStatus.OK);
    }

    // 기존 회원 가입 요청 삭제(거절)
    @DeleteMapping("/{clubId}/members/sign-up/{clubMemberAccountStatusId}")
    public ResponseEntity<ApiResponse> deleteSignUpRequest(@PathVariable("clubId") Long clubId, @PathVariable("clubMemberAccountStatusId") Long clubMemberAccountStatusId) {
        return new ResponseEntity<>(clubLeaderService.deleteSignUpRequest(clubId, clubMemberAccountStatusId), HttpStatus.OK);
    }
}