package com.USWCicrcleLink.server.clubLeader.service;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import com.USWCicrcleLink.server.aplict.dto.ApplicantResultsRequest;
import com.USWCicrcleLink.server.aplict.dto.ApplicantsResponse;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubMembers;
import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.clubLeader.dto.*;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.*;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.response.PageResponse;
import com.USWCicrcleLink.server.global.security.util.CustomLeaderDetails;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.User;
import com.USWCicrcleLink.server.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import com.USWCicrcleLink.server.global.util.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClubLeaderService {
    private final ClubRepository clubRepository;
    private final ClubIntroRepository clubIntroRepository;
    private final LeaderRepository leaderRepository;
    private final ClubMembersRepository clubMembersRepository;
    private final AplictRepository aplictRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    private final FileUploadService fileUploadService;
    private final FcmServiceImpl fcmService;

    // 대표 사진 경로
    @Value("${file.mainPhoto-dir}")
    private String mainPhotoDir;
    @Value("${file.introPhoto-dir}")
    private String introPhotoDir;

    // 동아리 기본 정보 조회
    @Transactional(readOnly = true)
    public ApiResponse<ClubInfoResponse> getClubInfo(Long clubId) {

        Club club = validateLeader(clubId);

        ClubInfoResponse clubInfoResponse = new ClubInfoResponse(
                club.getMainPhotoPath(),
                club.getClubName(),
                club.getLeaderName(),
                club.getLeaderHp(),
                club.getClubInsta()
        );

        return new ApiResponse<>("동아리 기본 정보 조회 완료", clubInfoResponse);
    }

    // 동아리 기본 정보 변경
    public ApiResponse updateClubInfo(Long clubId, ClubInfoRequest clubInfoRequest) throws IOException {

        Club club = validateLeader(clubId);

        // 사진 파일 업로드 과정
        createMainPhotoDir();// 사진 파일 디렉터리 없는 경우 생성

        // 파일 저장 경로
        String mainPhotoPath = fileUploadService.saveFile(clubInfoRequest.getMainPhoto(), club.getMainPhotoPath(), mainPhotoDir);

        // 동아리 정보 변경
        club.updateClubInfo(mainPhotoPath, clubInfoRequest.getLeaderName(), clubInfoRequest.getLeaderHp(),
                 clubInfoRequest.getClubInsta());

        clubRepository.save(club);
        log.debug("동아리 기본 정보 변경 완료: {}", club.getClubName());
        return new ApiResponse<>("동아리 기본 정보 변경 완료", club.getClubName());
    }

    // 동아리 소개 조회
    @Transactional(readOnly = true)
    public ApiResponse<ClubIntroResponse> getClubIntro(Long clubId) {

        Club club = validateLeader(clubId);
        ClubIntro clubIntro = clubIntroRepository.findByClubClubId(club.getClubId())
                .orElseThrow(() -> new ClubIntroException(ExceptionType.CLUB_INTRO_NOT_EXISTS));

        ClubIntroResponse clubIntroResponse = new ClubIntroResponse(
                club.getMainPhotoPath(),
                clubIntro.getClubIntroPhotoPath(),
                clubIntro.getAdditionalPhotoPath1(),
                clubIntro.getAdditionalPhotoPath2(),
                clubIntro.getAdditionalPhotoPath3(),
                clubIntro.getAdditionalPhotoPath4(),
                club.getClubName(),
                club.getLeaderName(),
                club.getLeaderHp(),
                club.getClubInsta(),
                clubIntro.getClubIntro()
        );

        return new ApiResponse<>("동아리 소개 조회 완료", clubIntroResponse);
    }

    // 동아리 소개 변경
    public ApiResponse updateClubIntro(Long clubId, ClubIntroRequest clubIntroRequest) throws IOException {

        Club club = validateLeader(clubId);

        // 사진 파일 업로드 과정
        createIntroPhotoDir();// 사진 파일 디렉터리 없는 경우 생성

        // 기존 파일 경로가 있는지 확인
        ClubIntro existingClubIntro = clubIntroRepository.findByClubClubId(club.getClubId())
                .orElseThrow(() -> new ClubIntroException(ExceptionType.CLUB_INTRO_NOT_EXISTS));

        // 파일 있나 ? 덮어쓰기 : 비워두기
        String introPhotoPath = fileUploadService.saveFile(clubIntroRequest.getIntroPhoto(),
                existingClubIntro.getClubIntroPhotoPath(), introPhotoDir);

        String additionalPhotoPath1 = fileUploadService.saveFile(clubIntroRequest.getAdditionalPhoto1(),
                existingClubIntro.getAdditionalPhotoPath1(), introPhotoDir);

        String additionalPhotoPath2 = fileUploadService.saveFile(clubIntroRequest.getAdditionalPhoto2(),
                existingClubIntro.getAdditionalPhotoPath2(), introPhotoDir);

        String additionalPhotoPath3 = fileUploadService.saveFile(clubIntroRequest.getAdditionalPhoto3(),
                existingClubIntro.getAdditionalPhotoPath3(), introPhotoDir);

        String additionalPhotoPath4 = fileUploadService.saveFile(clubIntroRequest.getAdditionalPhoto4(),
                existingClubIntro.getAdditionalPhotoPath4(), introPhotoDir);

        // 동아리 소개 저장
        existingClubIntro.updateClubIntro(club, clubIntroRequest.getClubIntro(), clubIntroRequest.getGoogleFormUrl(),
                introPhotoPath, additionalPhotoPath1, additionalPhotoPath2,
                additionalPhotoPath3, additionalPhotoPath4);

        clubIntroRepository.save(existingClubIntro);
        log.debug("동아리 소개 저장 완료: {}", existingClubIntro);
        return new ApiResponse<>("동아리 소개 변경 완료", club.getClubName());
    }

    // 사진 파일 저장 디렉터리 없는 경우 생성
    private void createMainPhotoDir() throws IOException {
        Files.createDirectories(Paths.get(mainPhotoDir));
    }

    private void createIntroPhotoDir() throws IOException {
        Files.createDirectories(Paths.get(introPhotoDir));
    }

    // 동아리 모집 상태 변경
    public ApiResponse toggleRecruitmentStatus(Long clubId) {

        Club club = validateLeader(clubId);

        ClubIntro clubIntro = clubIntroRepository.findByClubClubId(club.getClubId())
                .orElseThrow(() -> new ClubIntroException(ExceptionType.CLUB_INTRO_NOT_EXISTS));
        log.debug("동아리 소개 조회 결과: {}", clubIntro);

        // 모집 상태 현재와 반전
        clubIntro.toggleRecruitmentStatus();
        clubRepository.save(club);

        return new ApiResponse<>("동아리 모집 상태 변경 완료", clubIntro.getRecruitmentStatus());
    }

    // 소속 동아리원 조회(구, 성능 비교용)
//    @Transactional(readOnly = true)
//    public ApiResponse<List<ClubMembersResponse>> findClubMembers(LeaderToken token) {
//
//        Club club = validateLeader(token);
//
//        // 해당 동아리원 조회(성능 비교)
////        List<ClubMembers> findClubMembers = clubMembersRepository.findByClub(club); // 일반
//        List<ClubMembers> findClubMembers = clubMembersRepository.findAllWithProfile(club.getClubId()); // 성능
//
//        // 동아리원과 프로필 조회
//        List<ClubMembersResponse> memberProfiles = findClubMembers.stream()
//                .map(cm -> new ClubMembersResponse(
//                        cm.getClubMemberId(),
//                        cm.getProfile()
//                ))
//                .collect(toList());
//
//        return new ApiResponse<>("소속 동아리원 조회 완료", memberProfiles);
//    }

    // 소속 동아리원 조회
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse<ClubMembersResponse>> getClubMembers(Long clubId, int page, int size) {

        Club club = validateLeader(clubId);

        PageRequest pageable = PageRequest.of(page, size);

        Page<ClubMembers> findClubMembers = clubMembersRepository.findAllWithProfileByClubId(club.getClubId(), pageable);

        // 동아리원과 프로필 조회
        List<ClubMembersResponse> memberProfiles = findClubMembers.stream()
                .map(cm -> new ClubMembersResponse(
                        cm.getClubMemberId(),
                        cm.getProfile()
                ))
                .collect(toList());

        PageResponse<ClubMembersResponse> pageResponse = new PageResponse<>(
                memberProfiles,
                findClubMembers.getNumber(),
                findClubMembers.getSize(),
                findClubMembers.getTotalElements(),
                findClubMembers.getTotalPages()
        );

        return new ApiResponse<>("소속 동아리원 조회 완료", pageResponse);
    }

    // 소속 동아리원 삭제
    public ApiResponse deleteClubMember(Long clubMemberId, Long clubId) {

        Club club = validateLeader(clubId);

        // 동아리원 삭제
        clubMembersRepository.deleteById(clubMemberId);
        return new ApiResponse<>("동아리원 삭제 완료");
    }

    // 소속 동아리원 엑셀 다운
    @Transactional(readOnly = true)
    public void downloadExcel(Long clubId, HttpServletResponse response) {

        Club club = validateLeader(clubId);

        // 해당 동아리원 조회
        List<ClubMembers> findClubMembers = clubMembersRepository.findAllWithProfile(club.getClubId());

        // 동아리원의 프로필 조회 후 동아리원 정보로 정리
        List<ClubMembersExcelResponse> memberProfiles = findClubMembers.stream()
                .map(cm -> new ClubMembersExcelResponse(
                        cm.getProfile()
                ))
                .collect(toList());

        // 파일 이름 설정
        String fileName = club.getClubName() + "_회원_명단.xlsx";
        String encodedFileName;
        try {
            encodedFileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (IOException e) {
            throw new FileException(ExceptionType.FILE_ENCODING_FAILED);
        }

        // Content-Disposition 헤더 설정
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + encodedFileName);

        // 엑셀 파일 생성
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ) {
            // 시트 이름 설정
            Sheet sheet = workbook.createSheet(club.getClubName());

            // 표 바탕색
            CellStyle blueCellStyle = workbook.createCellStyle();
            applyCellStyle(blueCellStyle, new Color(74, 119, 202));

            // 표 시작 위치
            Row headerRow = sheet.createRow(0);

            // 카테고리 설정
            String[] columnHeaders = {"학과", "학번", "이름", "전화번호"};
            for (int i = 0; i < columnHeaders.length; i++) {// 셀 생성, 카테고리 부여
                Cell headerCell = headerRow.createCell(i);
                headerCell.setCellValue(columnHeaders[i]);
                headerCell.setCellStyle(blueCellStyle);
            }

            // DB값 엑셀 파일에 넣기
            int rowNum = 1;
            for (ClubMembersExcelResponse member : memberProfiles) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(member.getMajor());
                row.createCell(1).setCellValue(member.getStudentNumber());
                row.createCell(2).setCellValue(member.getUserName());
                row.createCell(3).setCellValue(member.getUserHp());
            }

            workbook.write(outputStream);
            outputStream.writeTo(response.getOutputStream());
            response.flushBuffer();
            log.debug("{} 파일 추출 완료", club.getClubName());
        } catch (IOException e) {
            throw new FileException(ExceptionType.FILE_CREATE_FAILED);
        }
    }

    // 엑셀 표 스타일 설정
    private void applyCellStyle(CellStyle cellStyle, Color color) {
        XSSFCellStyle xssfCellStyle = (XSSFCellStyle) cellStyle;
        xssfCellStyle.setFillForegroundColor(new XSSFColor(color, new DefaultIndexedColorMap()));
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 글 정렬
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 표 그리기
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
    }

    // 동아리 지원자 조회
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse> getApplicants(Long clubId, int page, int size) {
        Club club = validateLeader(clubId);

        Pageable pageable = PageRequest.of(page, size);

        // 합/불 처리되지 않은 동아리 지원자 조회
        Page<Aplict> aplicts = aplictRepository.findAllWithProfileByClubId(
                club.getClubId(),
                pageable,
                false);
        List<ApplicantsResponse> applicants = aplicts.stream()
                .map(ap -> new ApplicantsResponse(
                        ap.getId(),
                        ap.getProfile()
                ))
                .collect(toList());

        PageResponse<ApplicantsResponse> pageResponse = new PageResponse<>(
                applicants,
                aplicts.getNumber(),
                aplicts.getSize(),
                aplicts.getTotalElements(),
                aplicts.getTotalPages()
        );

        return new ApiResponse<>("지원자 조회 완료", pageResponse);
    }

    // 최초 합격자 알림
    public void updateApplicantResults(Long clubId, List<ApplicantResultsRequest> results) throws IOException {
        Club club = validateLeader(clubId);

        // 동아리 지원자 전원 조회(최초 합격)
        List<Aplict> applicants = aplictRepository.findByClub_ClubIdAndChecked(club.getClubId(), false);

        // 선택된 지원자 수와 전체 동아리 지원자 수 비교
        validateTotalApplicants(applicants, results);

        // 지원자 검증(지원한 동아리 + 지원서 + check안된 상태)
        for (ApplicantResultsRequest result : results) {
            Aplict applicant = aplictRepository.findByClub_ClubIdAndIdAndChecked(
                            club.getClubId(),
                            result.getAplictId(),
                            false)
                    .orElseThrow(() -> new AplictException(ExceptionType.APPLICANT_NOT_EXISTS));

            // 합격 불합격 상태 업데이트
            // 합/불, checked, 삭제 날짜

            AplictStatus aplictResult = result.getAplictStatus();// 지원 결과 PASS/ FAIL
            if (aplictResult == AplictStatus.PASS) {
                applicant.updateAplictStatus(aplictResult, true, LocalDateTime.now().plusDays(4));
                fcmService.sendMessageTo(applicant, aplictResult);
                log.debug("합격 처리 완료: {}", applicant.getId());
            } else if (aplictResult == AplictStatus.FAIL) {
                applicant.updateAplictStatus(aplictResult, true, LocalDateTime.now().plusDays(4));
                fcmService.sendMessageTo(applicant, aplictResult);
                log.debug("불합격 처리 완료: {}", applicant.getId());
            }

            aplictRepository.save(applicant);
        }
    }

    // 선택된 지원자 수와 전체 지원자 수 비교
    private void validateTotalApplicants(List<Aplict> applicants, List<ApplicantResultsRequest> results) {
        Set<Long> applicantIds = applicants.stream()
                .map(Aplict::getId)
                .collect(Collectors.toSet());

        Set<Long> requestedApplicantIds = results.stream()
                .map(ApplicantResultsRequest::getAplictId)
                .collect(Collectors.toSet());

        if (!requestedApplicantIds.equals(applicantIds)) {
            throw new AplictException(ExceptionType.APPLICANT_COUNT_MISMATCH);
        }
    }

    // 불합격자 조회
    @Transactional(readOnly = true)
    public ApiResponse<PageResponse> getFailedApplicants(Long clubId, int page, int size) {
        Club club = validateLeader(clubId);

        Pageable pageable = PageRequest.of(page, size);

        // 불합격자 동아리 지원자 조회
        Page<Aplict> aplicts = aplictRepository.findAllWithProfileByClubIdAndFailed(
                club.getClubId(),
                pageable,
                true,
                AplictStatus.FAIL);

        List<ApplicantsResponse> applicants = aplicts.stream()
                .map(ap -> new ApplicantsResponse(
                        ap.getId(),
                        ap.getProfile()
                ))
                .collect(toList());

        PageResponse<ApplicantsResponse> pageResponse = new PageResponse<>(
                applicants,
                aplicts.getNumber(),
                aplicts.getSize(),
                aplicts.getTotalElements(),
                aplicts.getTotalPages()
        );

        return new ApiResponse<>("불합격자 조회 완료", pageResponse);
    }

    // 동아리 지원자 추가 합격 처리
    public void updateFailedApplicantResults(Long clubId, List<ApplicantResultsRequest> results) throws IOException {
        Club club = validateLeader(clubId);

        // 지원자 검증(지원한 동아리 + 지원서 + check된 상태 + 불합)
        for (ApplicantResultsRequest result : results) {
            Aplict applicant = aplictRepository.findByClub_ClubIdAndIdAndCheckedAndAplictStatus(
                            club.getClubId(),
                            result.getAplictId(),
                            true,
                            AplictStatus.FAIL
                    )
                    .orElseThrow(() -> new AplictException(ExceptionType.ADDITIONAL_APPLICANT_NOT_EXISTS));

            // 합격 불합격 상태 업데이트
            // 합격
            AplictStatus aplictResult = result.getAplictStatus();
            applicant.updateFailedAplictStatus(aplictResult);
            fcmService.sendMessageTo(applicant, aplictResult);
            log.debug("합격 처리 완료: {}", applicant.getId());

            aplictRepository.save(applicant);
        }
    }

    public void updateFcmToken(FcmTokenTestRequest fcmTokenTestRequest) {
        User user = userRepository.findByUserAccount(fcmTokenTestRequest.getUserAccount())
                .orElseThrow(() -> new RuntimeException("유효한 회원이 없습니다."));

        Profile profile = profileRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("유효한 회원이 없습니다."));

        profile.updateFcmToken(fcmTokenTestRequest.getFcmToken());
        profileRepository.save(profile);
        log.debug("fcmToken 업데이트: {}", user.getUserAccount());
    }

    // 회장 검증 및 소속 동아리
    private Club validateLeader(Long clubId) {
        // SecurityContextHolder에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomLeaderDetails leaderDetails = (CustomLeaderDetails) authentication.getPrincipal();
        Leader leader = leaderDetails.leader();
        log.debug("인증된 동아리 회장: {}", leader.getLeaderAccount());

        // 동아리 조회
        Club club = clubRepository.findById(leader.getClub().getClubId())
                .orElseThrow(() -> new ClubException(ExceptionType.CLUB_NOT_EXISTS));
        log.debug("동아리 조회 결과: {}", club.getClubName());

        // 요청된 clubId와 인증된 회장의 clubId 비교
        if (!club.getClubId().equals(clubId)) {
            throw new ClubLeaderException(ExceptionType.CLUB_LEADER_ACCESS_DENIED);
        }

        return club;
    }
}