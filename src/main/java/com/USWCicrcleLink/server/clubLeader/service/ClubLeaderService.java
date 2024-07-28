package com.USWCicrcleLink.server.clubLeader.service;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.domain.ClubMembers;
import com.USWCicrcleLink.server.club.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.clubLeader.dto.*;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.response.ApiResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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
    private final FileUploadService fileUploadService;

    // 대표 사진 경로
    @Value("${file.mainPhoto-dir}")
    private String mainPhotoDir;
    @Value("${file.introPhoto-dir}")
    private String introPhotoDir;

    // 동아리 기본 정보 조회
    @Transactional(readOnly = true)
    public ApiResponse<ClubInfoResponse> getClubInfo(LeaderToken token) {

        Club club = validateLeader(token);

        ClubInfoResponse clubInfoResponse = new ClubInfoResponse(
                club.getMainPhotoPath(),
                club.getClubName(),
                club.getLeaderName(),
                club.getLeaderHp(),
                club.getKatalkID(),
                club.getClubInsta()
        );

        return new ApiResponse<>("동아리 기본 정보 조회 완료", clubInfoResponse);
    }

    // 동아리 기본 정보 변경
    public void updateClubInfo(LeaderToken token, ClubInfoRequest clubInfoRequest) throws IOException {

        Club club = validateLeader(token);

        // 사진 파일 업로드 과정
        createMainPhotoDir();// 사진 파일 디렉터리 없는 경우 생성

        // 파일 저장 경로
        String mainPhotoPath = fileUploadService.saveFile(clubInfoRequest.getMainPhoto(), club.getMainPhotoPath(), mainPhotoDir);

        // 동아리 정보 변경
        club.updateClubInfo(mainPhotoPath, clubInfoRequest.getLeaderName(), clubInfoRequest.getLeaderHp(),
                clubInfoRequest.getKatalkID(), clubInfoRequest.getClubInsta());
//        , clubInfoRequest.getchatRoomURL);

        clubRepository.save(club);
        log.info("동아리 기본 정보 변경 완료: {}", club.getClubName());
    }

    // 동아리 소개 변경
    public void updateClubIntro(LeaderToken token, ClubIntroRequest clubIntroRequest) throws IOException {

        Club club = validateLeader(token);

        // 사진 파일 업로드 과정
        createIntroPhotoDir();// 사진 파일 디렉터리 없는 경우 생성

        // 기존 파일 경로가 있는지 확인
        ClubIntro existingClubIntro = clubIntroRepository.findByClub(club)
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 소개가 아닙니다."));

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
        log.info("동아리 소개 저장 완료: {}", existingClubIntro);
    }

    // 사진 파일 저장 디렉터리 없는 경우 생성
    private void createMainPhotoDir() throws IOException {
        Files.createDirectories(Paths.get(mainPhotoDir));
    }

    private void createIntroPhotoDir() throws IOException {
        Files.createDirectories(Paths.get(introPhotoDir));
    }

    // 동아리 모집 상태 변경
    public RecruitmentStatus toggleRecruitmentStatus(LeaderToken token) {

        Club club = validateLeader(token);

        ClubIntro clubIntro = clubIntroRepository.findByClub(club)
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 소개가 아닙니다."));
        log.info("동아리 소개 조회 결과: {}", clubIntro);

        // 모집 상태 현재와 반전
        club.toggleRecruitmentStatus();

        return clubIntro.getRecruitmentStatus();
    }

    // 소속 동아리원 조회
    @Transactional(readOnly = true)
    public ApiResponse<List<ClubMembersResponse>> findClubMembers(LeaderToken token) {

        Club club = validateLeader(token);

        // 해당 동아리원 조회(성능 비교)
//        List<ClubMembers> findClubMembers = clubMembersRepository.findByClub(club); // 일반
        List<ClubMembers> findClubMembers = clubMembersRepository.findAllWithProfile(club.getClubId()); // 성능

        // 동아리원과 프로필 조회
        List<ClubMembersResponse> memberProfiles = findClubMembers.stream()
                .map(cm -> new ClubMembersResponse(
                        cm.getClubMemberId(),
                        cm.getProfile()
                ))
                .collect(toList());

        return new ApiResponse<>("소속 동아리원 조회 완료", memberProfiles);
    }

    // 소속 동아리원 삭제
    public ApiResponse deleteClubMember(Long clubMemberId, LeaderToken token) {

        Club club = validateLeader(token);

        // 동아리원 삭제
        clubMembersRepository.deleteById(clubMemberId);
        return new ApiResponse<>("동아리원 삭제 완료");
    }

    // 소속 동아리원 엑셀 다운
    @Transactional(readOnly = true)
    public void downloadExcel(LeaderToken token, HttpServletResponse response) {

        Club club = validateLeader(token);

        // 해당 동아리원 조회
        List<ClubMembers> findClubMembers = clubMembersRepository.findAllWithProfile(club.getClubId());

        // 동아리원의 프로필 조회 후 동아리원 정보로 정리
        List<ClubMembersExcelResponse> memberProfiles = findClubMembers.stream()
                .map(cm -> new ClubMembersExcelResponse(
                        cm.getProfile()
                ))
                .collect(toList());

        // 파일 이름 설정
        String fileName = club.getClubName() + " 회원 명단.xlsx";
        String encodedFileName;
        try {
            encodedFileName =URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString());
        } catch (IOException e) {
            throw new RuntimeException("파일 이름 인코딩에 실패했습니다.", e);
        }

        // Content-Disposition 헤더 설정
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);

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

        } catch (IOException e) {
            throw new RuntimeException("엑셀 파일 생성에 실패했습니다.", e);
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

    // 회장 검증 및 소속 동아리
    private Club validateLeader(LeaderToken token) {
        // 토큰 적용, 예외 처리 시 변경
        Leader leader = leaderRepository.findByLeaderUUID(token.getLeaderUUID())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 회장이 아닙니다."));
        log.info("동아리 회장 조회 결과: {}", leader);

        // 동아리 조회
        Club club = clubRepository.findById(leader.getClub().getClubId())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 아닙니다."));
        log.info("동아리 조회 결과: {}", club);

        return club;
    }
}