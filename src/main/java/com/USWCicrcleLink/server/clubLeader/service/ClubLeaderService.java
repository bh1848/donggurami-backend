package com.USWCicrcleLink.server.clubLeader.service;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import com.USWCicrcleLink.server.aplict.dto.ApplicantResultsRequest;
import com.USWCicrcleLink.server.aplict.dto.ApplicantsResponse;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.club.club.domain.*;
import com.USWCicrcleLink.server.club.club.repository.*;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntroPhoto;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroPhotoRepository;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.clubLeader.dto.*;
import com.USWCicrcleLink.server.clubLeader.util.ClubMemberExcelDataDto;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.*;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.global.response.PageResponse;
import com.USWCicrcleLink.server.global.security.util.CustomLeaderDetails;
import com.USWCicrcleLink.server.global.util.s3File.Service.S3FileUploadService;
import com.USWCicrcleLink.server.global.util.s3File.dto.S3FileResponse;
import com.USWCicrcleLink.server.global.util.validator.FileSignatureValidator;
import com.USWCicrcleLink.server.global.util.validator.InputValidator;
import com.USWCicrcleLink.server.profile.domain.MemberType;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.ClubMemberAccountStatus;
import com.USWCicrcleLink.server.user.repository.ClubMemberAccountStatusRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClubLeaderService {
    private final ClubRepository clubRepository;
    private final ClubIntroRepository clubIntroRepository;
    private final ClubMembersRepository clubMembersRepository;
    private final AplictRepository aplictRepository;
    private final ClubIntroPhotoRepository clubIntroPhotoRepository;
    private final ClubMainPhotoRepository clubMainPhotoRepository;
    private final ProfileRepository profileRepository;
    private final ClubHashtagRepository clubHashtagRepository;
    private final ClubCategoryRepository clubCategoryRepository;
    private final ClubCategoryMappingRepository clubCategoryMappingRepository;

    private final S3FileUploadService s3FileUploadService;
    private final FcmServiceImpl fcmService;
    private final ClubMemberAccountStatusRepository clubMemberAccountStatusRepository;

    // 최대 사진 순서(업로드, 삭제)
    int PHOTO_LIMIT = 5;

    private final String S3_MAINPHOTO_DIR = "mainPhoto/";
    private final String S3_INTROPHOTO_DIR = "introPhoto/";

    // 동아리 기본 정보 조회
    @Transactional(readOnly = true)
    public ApiResponse<ClubInfoResponse> getClubInfo(Long clubId) {

        Club club = validateLeader(clubId);

        Optional<ClubMainPhoto> clubMainPhoto = Optional.ofNullable(clubMainPhotoRepository.findByClub_ClubId(club.getClubId()));

        // 사진이 있으면 url 없으면 null
        String mainPhotoUrl = clubMainPhoto.map(
                        photo -> s3FileUploadService.generatePresignedGetUrl(photo.getClubMainPhotoS3Key()))
                .orElse(null);

        // clubHashtag 조회
        List<String> clubHashtags = clubHashtagRepository.findByClubClubId(club.getClubId())
                .stream().map(ClubHashtag::getClubHashtag).collect(toList());

        // clubCategory 조회
        List<String> clubCategories = clubCategoryMappingRepository.findByClubClubId(club.getClubId())
                .stream().map(mapping -> mapping.getClubCategory().getClubCategory()).collect(toList());

        ClubInfoResponse clubInfoResponse = new ClubInfoResponse(
                mainPhotoUrl,
                club.getClubName(),
                club.getLeaderName(),
                club.getLeaderHp(),
                club.getClubInsta(),
                club.getClubRoomNumber(),
                clubHashtags,
                clubCategories,
                club.getDepartment()
        );

        return new ApiResponse<>("동아리 기본 정보 조회 완료", clubInfoResponse);
    }

    // 동아리 기본 정보 변경
    public ApiResponse<UpdateClubInfoResponse> updateClubInfo(Long clubId, ClubInfoRequest clubInfoRequest, MultipartFile mainPhoto) throws IOException {
        // 동아리 회장 유효성 검증
        Club club = validateLeader(clubId);

        // 입력값 검증 (XSS 공격 방지)
        String sanitizedLeaderName = InputValidator.sanitizeContent(clubInfoRequest.getLeaderName());
        String sanitizedLeaderHp = InputValidator.sanitizeContent(clubInfoRequest.getLeaderHp());
        String sanitizedClubInsta = InputValidator.sanitizeContent(clubInfoRequest.getClubInsta());

        // 동아리 해시태그 처리
        if (clubInfoRequest.getClubHashtag() != null) {
            // 기존 해시태그 삭제
            clubHashtagRepository.deleteByClub_ClubId(clubId);

            // 새로운 해시태그 저장
            for (String hashtag : clubInfoRequest.getClubHashtag()) {
                ClubHashtag clubHashtag = ClubHashtag.builder()
                        .club(club)
                        .clubHashtag(InputValidator.sanitizeContent(hashtag))
                        .build();
                clubHashtagRepository.save(clubHashtag);
            }
        }

        // 동아리 카테고리 처리
        if (clubInfoRequest.getClubCategory() != null) {
            // 기존 카테고리 매핑 삭제
            clubCategoryMappingRepository.deleteByClub_ClubId(clubId);

            // 새로운 카테고리 매핑 저장
            for (String category : clubInfoRequest.getClubCategory()) {
                ClubCategory clubCategory = clubCategoryRepository.findByClubCategory(category)
                        .orElseThrow(() -> new ClubException(ExceptionType.INVALID_CATEGORY));

                ClubCategoryMapping categoryMapping = ClubCategoryMapping.builder()
                        .club(club)
                        .clubCategory(clubCategory)
                        .build();
                clubCategoryMappingRepository.save(categoryMapping);
            }
        }

        // 사진 처리
        S3FileResponse s3FileResponse = null;
        if (mainPhoto != null && !mainPhoto.isEmpty()) {
            // 기존 동아리 대표 사진 조회
            ClubMainPhoto existingPhoto = clubMainPhotoRepository.findByClub_ClubId(clubId);

            // 기존 사진 삭제
            if (existingPhoto != null && existingPhoto.getClubMainPhotoS3Key() != null && !existingPhoto.getClubMainPhotoS3Key().isEmpty()) {
                s3FileUploadService.deleteFile(existingPhoto.getClubMainPhotoS3Key());
                log.debug("기존 대표 사진 삭제 완료: {}", existingPhoto.getClubMainPhotoS3Key());
            }

            // 새로운 파일 업로드 및 메타 데이터 업데이트
            s3FileResponse = updateClubMainPhotoAndS3File(mainPhoto, existingPhoto);
        }

        // 동아리 기본 정보 업데이트
        club.updateClubInfo(sanitizedLeaderName, sanitizedLeaderHp, sanitizedClubInsta, clubInfoRequest.getClubRoomNumber());
        clubRepository.save(club);
        log.debug("동아리 기본 정보 변경 완료: {}", club.getClubName());

        // 응답 생성
        String mainPhotoUrl = s3FileResponse != null ? s3FileResponse.getPresignedUrl() : "";
        return new ApiResponse<>("동아리 기본 정보 변경 완료", new UpdateClubInfoResponse(mainPhotoUrl));
    }

    private S3FileResponse updateClubMainPhotoAndS3File(MultipartFile mainPhoto, ClubMainPhoto existingPhoto) throws IOException {
        // 새로운 파일 업로드
        S3FileResponse s3FileResponse = s3FileUploadService.uploadFile(mainPhoto, S3_MAINPHOTO_DIR);

        // s3key 및 photoname 업데이트
        existingPhoto.updateClubMainPhoto(mainPhoto.getOriginalFilename(), s3FileResponse.getS3FileName());
        clubMainPhotoRepository.save(existingPhoto);
        log.debug("기존 대표 사진 업데이트 완료: {}", s3FileResponse.getS3FileName());

        return s3FileResponse;
    }

    // 자신의 동아리 상세 페이지 조회(웹)
    @Transactional(readOnly = true)
    public ClubIntroWebResponse getClubIntro(Long clubId) {
        Club club = validateLeader(clubId);

        // 동아리 소개 조회
        ClubIntro clubIntro = clubIntroRepository.findByClubClubId(club.getClubId())
                .orElseThrow(() -> new ClubIntroException(ExceptionType.CLUB_INTRO_NOT_EXISTS));

        // clubHashtag 조회
        List<String> clubHashtags = clubHashtagRepository.findByClubClubId(club.getClubId())
                .stream().map(ClubHashtag::getClubHashtag).collect(toList());

        // 동아리 메인 사진 조회
        ClubMainPhoto clubMainPhoto = clubMainPhotoRepository.findByClub(club).orElse(null);

        // 동아리 소개 사진 조회
        List<ClubIntroPhoto> clubIntroPhotos = clubIntroPhotoRepository.findByClubIntro(clubIntro);

        // S3에서 메인 사진 URL 생성 (기본 URL 또는 null 처리)
        String mainPhotoUrl = (clubMainPhoto != null)
                ? s3FileUploadService.generatePresignedGetUrl(clubMainPhoto.getClubMainPhotoS3Key())
                : null;

        // S3에서 소개 사진 URL 생성 (소개 사진이 없을 경우 빈 리스트)
        List<String> introPhotoUrls = clubIntroPhotos.isEmpty()
                ? Collections.emptyList()
                : clubIntroPhotos.stream()
                .sorted(Comparator.comparingInt(ClubIntroPhoto::getOrder))
                .map(photo -> s3FileUploadService.generatePresignedGetUrl(photo.getClubIntroPhotoS3Key()))
                .collect(Collectors.toList());

        // ClubIntroResponse 반환
        return new ClubIntroWebResponse(club, clubHashtags, clubIntro, mainPhotoUrl, introPhotoUrls);
    }

    // 동아리 소개 변경
    public ApiResponse updateClubIntro(Long clubId, ClubIntroRequest clubIntroRequest, List<MultipartFile> introPhotos) throws IOException {

        Club club = validateLeader(clubId);

        ClubIntro clubIntro = clubIntroRepository.findByClubClubId(club.getClubId())
                .orElseThrow(() -> new ClubIntroException(ExceptionType.CLUB_INTRO_NOT_EXISTS));

        // 모집 상태가 null일 때 예외 처리
        if (clubIntroRequest.getRecruitmentStatus() == null) {
            throw new ClubIntroException(ExceptionType.INVALID_RECRUITMENT_STATUS);
        }

        // 입력값 검증 (XSS 공격 방지)
        String sanitizedClubIntro = clubIntroRequest.getClubIntro() != null
                ? InputValidator.sanitizeContent(clubIntroRequest.getClubIntro()) : "";
        String sanitizedClubRecruitment = clubIntroRequest.getClubRecruitment() != null
                ? InputValidator.sanitizeContent(clubIntroRequest.getClubRecruitment()) : "";
        String sanitizedGoogleFormUrl = clubIntroRequest.getGoogleFormUrl() != null
                ? InputValidator.sanitizeContent(clubIntroRequest.getGoogleFormUrl()) : "";

        // 삭제할 사진 확인
        if (clubIntroRequest.getDeletedOrders() != null && !clubIntroRequest.getDeletedOrders().isEmpty()) {
            // 순서 개수, 범위 검증
            validateOrderValues(clubIntroRequest.getDeletedOrders());

            for (int i = 0; i < clubIntroRequest.getDeletedOrders().size(); i++) {// 하나씩 삭제
                int deletingOrder = clubIntroRequest.getDeletedOrders().get(i);

                ClubIntroPhoto deletingPhoto = clubIntroPhotoRepository
                        .findByClubIntro_ClubIntroIdAndOrder(clubIntro.getClubIntroId(), deletingOrder)
                        .orElseThrow(() -> new ClubPhotoException(ExceptionType.PHOTO_ORDER_MISS_MATCH));

                s3FileUploadService.deleteFile(deletingPhoto.getClubIntroPhotoS3Key());

                deletingPhoto.updateClubIntroPhoto("", "", deletingOrder);
                clubIntroPhotoRepository.save(deletingPhoto);

                log.debug("소개 사진 삭제 완료: {}", deletingPhoto.getOrder());
            }
        }

        // 각 사진의 조회 presignedUrls
        List<String> presignedUrls = new ArrayList<>();

        // 동아리 소개 사진을 넣을 경우
        if (introPhotos != null && !introPhotos.isEmpty() && clubIntroRequest.getOrders() != null && !clubIntroRequest.getOrders().isEmpty()) {

            // 순서 개수, 범위 검증
            validateOrderValues(clubIntroRequest.getOrders());

            if (introPhotos.size() > PHOTO_LIMIT) {// 최대 5장 업로드
                throw new FileException(ExceptionType.MAXIMUM_FILE_LIMIT_EXCEEDED);
            }

            // N번째 사진 1장씩
            for (int i = 0; i < introPhotos.size(); i++) {
                MultipartFile introPhoto = introPhotos.get(i);
                int order = clubIntroRequest.getOrders().get(i);

                // 동아리 소개 사진이 존재하지 않으면 순서 스킵
                if (introPhoto == null || introPhoto.isEmpty()) {
                    continue;
                }

                ClubIntroPhoto existingPhoto = clubIntroPhotoRepository
                        .findByClubIntro_ClubIntroIdAndOrder(clubIntro.getClubIntroId(), order)
                        .orElseThrow(() -> new ClubPhotoException(ExceptionType.PHOTO_ORDER_MISS_MATCH));

                S3FileResponse s3FileResponse;

                // N번째 동아리 소개 사진 존재할 경우
                if (!existingPhoto.getClubIntroPhotoName().isEmpty() && !existingPhoto.getClubIntroPhotoS3Key().isEmpty()) {
                    // 기존 S3 파일 삭제
                    s3FileUploadService.deleteFile(existingPhoto.getClubIntroPhotoS3Key());
                    log.debug("기존 소개 사진 삭제 완료: {}", existingPhoto.getClubIntroPhotoS3Key());
                }
                // 새로운 파일 업로드 및 메타 데이터 업데이트
                s3FileResponse = updateClubIntroPhotoAndS3File(introPhoto, existingPhoto, order);

                // 업로드된 사진의 사전 서명된 URL을 리스트에 추가
                presignedUrls.add(s3FileResponse.getPresignedUrl());
            }
        }

        // 소개 글, 모집 글, google form 저장
        clubIntro.updateClubIntro(sanitizedClubIntro, sanitizedClubRecruitment, sanitizedGoogleFormUrl);
        clubIntroRepository.save(clubIntro);

        log.debug("{} 동아리 소개 변경 완료", club.getClubName());
        return new ApiResponse<>("동아리 소개 변경 완료", new UpdateClubIntroResponse(presignedUrls));
    }

    private void validateOrderValues(List<Integer> orders) {
        // 순서 개수 체크
        if (orders.size() < 1 || orders.size() > PHOTO_LIMIT) {// 0 이하 6이상
            throw new ClubPhotoException(ExceptionType.PHOTO_ORDER_MISS_MATCH);
        }

        // 순서 값
        for (int order : orders) {
            if (order < 1 || order > PHOTO_LIMIT) { // 1 ~ 5 사이여야 함
                new ClubPhotoException(ExceptionType.PHOTO_ORDER_MISS_MATCH);
            }
        }

    }

    private S3FileResponse updateClubIntroPhotoAndS3File(MultipartFile introPhoto, ClubIntroPhoto existingPhoto, int order) throws IOException {
        // 새로운 파일 업로드
        S3FileResponse s3FileResponse = s3FileUploadService.uploadFile(introPhoto, S3_INTROPHOTO_DIR);

        // s3key 및 photoname 업데이트
        existingPhoto.updateClubIntroPhoto(introPhoto.getOriginalFilename(), s3FileResponse.getS3FileName(), order);
        clubIntroPhotoRepository.save(existingPhoto);
        log.debug("사진 정보 저장 및 업데이트 완료: {}", s3FileResponse.getS3FileName());

        return s3FileResponse;
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

    // 소속 동아리 회원 조회(가나다순 정렬)
    @Transactional(readOnly = true)
    public ApiResponse<List<ClubMembersResponse>> getClubMembers(Long clubId) {

        Club club = validateLeader(clubId);

        List<ClubMembers> findClubMembers = clubMembersRepository.findAllWithProfileByName(clubId);

        // 동아리원과 프로필 조회
        List<ClubMembersResponse> memberProfiles = findClubMembers.stream()
                .map(cm -> new ClubMembersResponse(
                        cm.getClubMemberId(),
                        cm.getProfile()
                ))
                .collect(toList());

        return new ApiResponse<>("소속 동아리 회원 조회 완료", memberProfiles);
    }

    // 소속 동아리 회원 조회(정회원/ 비회원 정렬)
    @Transactional(readOnly = true)
    public ApiResponse<List<ClubMembersResponse>> getClubMembersByMemberType(Long clubId, MemberType memberType) {

        Club club = validateLeader(clubId);

        List<ClubMembers> findClubMembers = clubMembersRepository.findAllWithProfileByMemberType(clubId, memberType);

        // 동아리원과 프로필 조회
        List<ClubMembersResponse> memberProfiles = findClubMembers.stream()
                .map(cm -> new ClubMembersResponse(
                        cm.getClubMemberId(),
                        cm.getProfile()
                ))
                .collect(toList());

        return new ApiResponse<>("소속 동아리 회원 조회 완료", memberProfiles);
    }

    // 소속 동아리원 삭제
    public ApiResponse deleteClubMembers(Long clubId, List<ClubMembersDeleteRequest> clubMemberIdList) {

        Club club = validateLeader(clubId);

        List<Long> clubMemberIds = clubMemberIdList.stream()
                .map(ClubMembersDeleteRequest::getClubMemberId)
                .collect(toList());

        // 동아리 회원인지 확인
        List<ClubMembers> membersToDelete = clubMembersRepository.findByClubClubIdAndClubMemberIdIn(club.getClubId(), clubMemberIds);

        // 조회된 수와 요청한 수와 같은지(다르면 다른 동아리 회원이 존재)
        if (membersToDelete.size() != clubMemberIdList.size()) {
            throw new ClubMemberException(ExceptionType.CLUB_MEMBER_NOT_EXISTS);
        }

        // 동아리 회원 삭제
        clubMembersRepository.deleteAll(membersToDelete);
        return new ApiResponse<>("동아리 회원 삭제 완료", clubMemberIdList);
    }

    // 소속 동아리원 엑셀 다운
    @Transactional(readOnly = true)
    public void downloadExcel(Long clubId, HttpServletResponse response) {

        Club club = validateLeader(clubId);

        // 해당 동아리원 조회
        List<ClubMembers> findClubMembers = clubMembersRepository.findAllWithProfile(club.getClubId());

        // 동아리원의 프로필 조회 후 동아리원 정보로 정리
        List<ClubMembersExportExcelResponse> memberProfiles = findClubMembers.stream()
                .map(cm -> new ClubMembersExportExcelResponse(
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
            for (ClubMembersExportExcelResponse member : memberProfiles) {
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
                        ap.getAplictId(),
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
            Aplict applicant = aplictRepository.findByClub_ClubIdAndAplictIdAndChecked(
                            club.getClubId(),
                            result.getAplictId(),
                            false)
                    .orElseThrow(() -> new BaseException(ExceptionType.APPLICANT_NOT_EXISTS));

            // 동아리 회원 중복 검사
            checkDuplicateClubMember(applicant.getProfile().getProfileId(), club.getClubId());

            // 합격 불합격 상태 업데이트
            // 합/불, checked, 삭제 날짜
            AplictStatus aplictResult = result.getAplictStatus();// 지원 결과 PASS/ FAIL
            if (aplictResult == AplictStatus.PASS) {
                ClubMembers newClubMembers = ClubMembers.builder()
                        .club(club)
                        .profile(applicant.getProfile())
                        .build();
                applicant.updateAplictStatus(aplictResult, true, LocalDateTime.now().plusDays(4));
                clubMembersRepository.save(newClubMembers);
                log.debug("합격 처리 완료: {}", applicant.getAplictId());
            } else if (aplictResult == AplictStatus.FAIL) {
                applicant.updateAplictStatus(aplictResult, true, LocalDateTime.now().plusDays(4));
                log.debug("불합격 처리 완료: {}", applicant.getAplictId());
            }

            aplictRepository.save(applicant);
            fcmService.sendMessageTo(applicant, aplictResult);
        }
    }

    // 동아리 회원 중복 검사
    private void checkDuplicateClubMember(Long profileId, Long clubId) {
        boolean isDuplicate = clubMembersRepository
                .findByProfileProfileIdAndClubClubId(profileId, clubId)
                .isPresent();

        if (isDuplicate) {
            throw new ClubMemberException(ExceptionType.CLUB_MEMBER_ALREADY_EXISTS);
        }
    }

    // 선택된 지원자 수와 전체 지원자 수 비교
    private void validateTotalApplicants(List<Aplict> applicants, List<ApplicantResultsRequest> results) {
        Set<Long> applicantIds = applicants.stream()
                .map(Aplict::getAplictId)
                .collect(Collectors.toSet());

        Set<Long> requestedApplicantIds = results.stream()
                .map(ApplicantResultsRequest::getAplictId)
                .collect(Collectors.toSet());

        if (!requestedApplicantIds.equals(applicantIds)) {
            throw new BaseException(ExceptionType.APPLICANT_COUNT_MISMATCH);
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
                        ap.getAplictId(),
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
            Aplict applicant = aplictRepository.findByClub_ClubIdAndAplictIdAndCheckedAndAplictStatus(
                            club.getClubId(),
                            result.getAplictId(),
                            true,
                            AplictStatus.FAIL
                    )
                    .orElseThrow(() -> new BaseException(ExceptionType.ADDITIONAL_APPLICANT_NOT_EXISTS));

            // 동아리 회원 중복 검사
            checkDuplicateClubMember(applicant.getProfile().getProfileId(), club.getClubId());

            // 합격 불합격 상태 업데이트
            // 합격
            ClubMembers newClubMembers = ClubMembers.builder()
                    .club(club)
                    .profile(applicant.getProfile())
                    .build();
            clubMembersRepository.save(newClubMembers);

            AplictStatus aplictResult = result.getAplictStatus();
            applicant.updateFailedAplictStatus(aplictResult);
            aplictRepository.save(applicant);

            fcmService.sendMessageTo(applicant, aplictResult);
            log.debug("추가 합격 처리 완료: {}", applicant.getAplictId());
        }
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

    // 기존 동아리원 가져오기(엑셀 파일)
    @Transactional(readOnly = true)
    public ApiResponse<List<ClubMembersImportExcelResponse>> uploadExcel(Long clubId, MultipartFile clubMembersFile) throws IOException {
        Club club = validateLeader(clubId);

        // 엑셀 파일의 개수 확인
        if (clubMembersFile == null || clubMembersFile.isEmpty()) {
            throw new FileException(ExceptionType.MAXIMUM_FILE_LIMIT_EXCEEDED);
        }

        // 파일 확장자 확인
        String fileExtension = validateClubMembersExcelFile(clubMembersFile);

        // 엑셀 파일 확장자(구, 신버전)
        Workbook workbook;
        if (fileExtension.equals("xls")) {// 엑셀 버전 ~03
            workbook = new HSSFWorkbook(clubMembersFile.getInputStream());
        } else {// 엑셀 버전 07~
            workbook = new XSSFWorkbook(clubMembersFile.getInputStream());
        }

        Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트 사용
        // 추가 회원
        List<ClubMembersImportExcelResponse> excelClubMembers = new ArrayList<>();
        // 중복 회원
        List<Map<String, String>> duplicateUsers = new ArrayList<>();

        // 엑셀 데이터를 읽어 이름, 학번, 전화번호 수집
        Set<String> userNames = new HashSet<>();
        Set<String> studentNumbers = new HashSet<>();
        Set<String> userHpNumbers = new HashSet<>();
        // 이름_학번_전화번호를 키로 원본 데이터 저장
        Map<String, ClubMemberExcelDataDto> rowExcelDataMap = new HashMap<>();

        // 엑셀 파일 읽기
        for (int i = 1; i <= sheet.getLastRowNum(); i++) { // 첫 번째 행(헤더) 건너뛰기
            Row row = sheet.getRow(i);
            // 빈 줄은 무시
            if (row == null || isRowEmpty(row)) {
                continue;
            }

            // 셀 읽기
            String userName = getCellValue(row.getCell(0)).replaceAll("\\s+", ""); // 이름
            String studentNumber = getCellValue(row.getCell(1)).replaceAll("\\s+", ""); // 학번
            String userHp = getCellValue(row.getCell(2)).replaceAll("-", "").replaceAll("\\s+", ""); // 전화번호

            // 데이터 수집 후 한번에 조회
            userNames.add(userName);
            studentNumbers.add(studentNumber);
            userHpNumbers.add(userHp);
            rowExcelDataMap.put(userName + "_" + studentNumber + "_" + userHp, new ClubMemberExcelDataDto(userName, studentNumber, userHp));
        }
        // DB에서 중복 데이터 한 번에 확인
        List<Profile> duplicateProfiles = profileRepository.findByUserNameInAndStudentNumberInAndUserHpIn(userNames, studentNumbers, userHpNumbers);

        // 중복 데이터 매핑
        for (Profile profile : duplicateProfiles) {
            // 엑셀 데이터를 기반으로 매핑
            String duplicateProfileKey = profile.getUserName() + "_" + profile.getStudentNumber() + "_" + profile.getUserHp();// key
            ClubMemberExcelDataDto duplicateExcelData = rowExcelDataMap.get(duplicateProfileKey);// map 검색
            if (duplicateExcelData != null) {
                duplicateUsers.add(Map.of(
                        "이름", profile.getUserName(),
                        "학번", profile.getStudentNumber(),
                        "전화번호", profile.getUserHp()
                ));
            }
            // 확인한 key:value 삭제
            rowExcelDataMap.remove(duplicateProfileKey);
        }

        // 중복된 데이터가 있으면 예외 처리
        if (!duplicateUsers.isEmpty()) {
            throw new ProfileException(ExceptionType.DUPLICATE_PROFILE, duplicateUsers);
        }

        // 중복이 아닌 데이터 추가
        for (ClubMemberExcelDataDto rowData : rowExcelDataMap.values()) {
            excelClubMembers.add(new ClubMembersImportExcelResponse(rowData.getStudentNumber(), rowData.getUserName(), rowData.getUserHp()));
        }

        return new ApiResponse<>("기존 동아리 회원 엑셀로 가져오기 완료", excelClubMembers);
    }

    private String validateClubMembersExcelFile(MultipartFile clubMembersFile) {
        // 파일 확장자 확인
        String fileExtension = FilenameUtils.getExtension(clubMembersFile.getOriginalFilename());
        if (!fileExtension.equals("xls") && !fileExtension.equals("xlsx")) {
            throw new FileException(ExceptionType.UNSUPPORTED_FILE_EXTENSION);
        }

        // 파일 시그니처를 통해 실제 파일 형식이 올바른지 확인
        try {
            if (!FileSignatureValidator.isValidFileType(clubMembersFile.getInputStream(), fileExtension)) {
                throw new FileException(ExceptionType.UNSUPPORTED_FILE_EXTENSION);
            }
        } catch (IOException e) {
            throw new FileException(ExceptionType.FILE_VALIDATION_FAILED);
        }

        return fileExtension;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return "";
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    // 기존 동아리원 추가(엑셀)
    public void addClubMembersFromExcel(Long clubId, List<ClubMembersAddFromExcelRequest> clubMembersAddFromExcelRequests) {
        Club club = validateLeader(clubId);

        // 중복 확인 데이터 수집
        Map<String, ClubMembersAddFromExcelRequest> requestDataMap = new HashMap<>();
        List<Map<String, String>> duplicateUsers = new ArrayList<>();

        // 요청 데이터를 키로 매핑 (이름_학번_전화번호_전공)
        for (ClubMembersAddFromExcelRequest request : clubMembersAddFromExcelRequests) {
            if (request.getMajor() == null || request.getMajor().trim().isEmpty()) {
                throw new ProfileException(ExceptionType.DEPARTMENT_NOT_INPUT);
            }

            String clubMemberKey = request.getUserName() + "_"
                    + request.getStudentNumber() + "_"
                    + request.getUserHp() + "_"
                    + request.getMajor();
            requestDataMap.put(clubMemberKey, request);
        }

        // DB에서 중복 데이터 확인 (이름, 학번, 전화번호, 전공을 모두 포함)
        List<Profile> duplicateProfiles = profileRepository.findByUserNameInAndStudentNumberInAndUserHpInAndMajorIn(
                requestDataMap.values().stream().map(ClubMembersAddFromExcelRequest::getUserName).collect(Collectors.toSet()),
                requestDataMap.values().stream().map(ClubMembersAddFromExcelRequest::getStudentNumber).collect(Collectors.toSet()),
                requestDataMap.values().stream().map(ClubMembersAddFromExcelRequest::getUserHp).collect(Collectors.toSet()),
                requestDataMap.values().stream().map(ClubMembersAddFromExcelRequest::getMajor).collect(Collectors.toSet())
        );

        // 중복 확인 및 매핑
        for (Profile profile : duplicateProfiles) {
            String uniqueKey = profile.getUserName() + "_" + profile.getStudentNumber() + "_" + profile.getUserHp() + "_" + profile.getMajor();

            ClubMembersAddFromExcelRequest duplicateRequest = requestDataMap.get(uniqueKey);
            if (duplicateRequest != null) {
                duplicateUsers.add(Map.of(
                        "이름", profile.getUserName(),
                        "학번", profile.getStudentNumber(),
                        "전화번호", profile.getUserHp(),
                        "전공", profile.getMajor()
                ));
                requestDataMap.remove(uniqueKey); // 중복 데이터는 저장 대상에서 제거
            }
        }

        // 중복된 데이터가 있으면 예외 처리
        if (!duplicateUsers.isEmpty()) {
            throw new ProfileException(ExceptionType.DUPLICATE_PROFILE, duplicateUsers);
        }

        // 중복되지 않은 데이터만 저장
        for (ClubMembersAddFromExcelRequest validRequest : requestDataMap.values()) {
            Profile profile = Profile.builder()
                    .userName(validRequest.getUserName())
                    .studentNumber(validRequest.getStudentNumber())
                    .userHp(validRequest.getUserHp())
                    .major(validRequest.getMajor())
                    .profileCreatedAt(LocalDateTime.now())
                    .profileUpdatedAt(LocalDateTime.now())
                    .build();
            profileRepository.save(profile);

            ClubMembers clubMember = ClubMembers.builder()
                    .club(club)
                    .profile(profile)
                    .build();
            clubMembersRepository.save(clubMember);
        }
    }

    // 프로필 중복 동아리 회원 추가
    public ApiResponse addDuplicateProfileMember(Long clubId, DuplicateProfileMemberRequest duplicateProfileMemberRequest) {
        Club club = validateLeader(clubId);

        // 프로필 중복 회원 조회
        Profile duplicateProfile = profileRepository
                .findByUserNameAndStudentNumberAndUserHp(
                        duplicateProfileMemberRequest.getUserName(),
                        duplicateProfileMemberRequest.getStudentNumber(),
                        duplicateProfileMemberRequest.getUserHp()
                ).orElseThrow(() -> new ProfileException(ExceptionType.PROFILE_NOT_EXISTS));

        // 동아리 회원 중복 검사
        checkDuplicateClubMember(duplicateProfile.getProfileId(), club.getClubId());

        // 존재하면 동아리 회원으로 추가
        ClubMembers duplicateProfileClubMember = ClubMembers.builder()
                .club(club)
                .profile(duplicateProfile)
                .build();

        clubMembersRepository.save(duplicateProfileClubMember);
        return new ApiResponse<>("프로필 중복 동아리 회원 추가 완료", duplicateProfileMemberRequest);
    }

    // 비회원 프로필 업데이트
    public ApiResponse updateNonMemberProfile(Long clubId,
                                              Long clubMemberId,
                                              ClubNonMemberUpdateRequest request) {
        Club club = validateLeader(clubId);

        // 동아리 회원 확인
        ClubMembers clubMember = clubMembersRepository.findByClubClubIdAndClubMemberId(club.getClubId(), clubMemberId)
                .orElseThrow(() -> new ClubMemberException(ExceptionType.CLUB_MEMBER_NOT_EXISTS));

        // 비회원 확인
        if (clubMember.getProfile().getMemberType() != MemberType.NONMEMBER) {
            throw new ClubMemberException(ExceptionType.NOT_NON_MEMBER);
        }

        // 프로필 업데이트
        Profile profile = clubMember.getProfile();
        profile.updateProfile(request.getUserName(), request.getStudentNumber(), request.getUserHp(), request.getMajor());
        profileRepository.save(profile);

        return new ApiResponse("비회원 프로필 업데이트 완료", request);
    }

    // 기존 동아리 회원 가입 요청 조회
    public ApiResponse getSignUpRequest(Long clubId) {
        Club club = validateLeader(clubId);

        List<ClubMemberAccountStatus> signUpClubMember = clubMemberAccountStatusRepository.findAllWithClubMemberTemp(club.getClubId());
        List<SignUpRequestResponse> signUpRequestResponse = signUpClubMember.stream().map(
                cmt -> new SignUpRequestResponse(
                        cmt.getId(),
                        cmt.getClubMemberTemp()
                )
        ).toList();

        return new ApiResponse("기존 동아리 회원 가입 요청 조회 완료", signUpRequestResponse);
    }

    // 기존 동아리 회원 가입 요청 삭제
    public ApiResponse deleteSignUpRequest(Long clubId, Long clubMemberAccountStatusId) {
        Club club = validateLeader(clubId);

        // 동아리 + 기존 동아리 회원 가입 요청 확인
        ClubMemberAccountStatus clubMemberAccountStatus = clubMemberAccountStatusRepository.findByIdAndClubClubId(clubMemberAccountStatusId, club.getClubId())
                .orElseThrow(() -> new ClubMemberAccountStatusException(ExceptionType.CLUB_MEMBER_TEMP_NOT_EXISTS));

        clubMemberAccountStatusRepository.delete(clubMemberAccountStatus);
        return new ApiResponse("기존 동아리 회원 가입 요청 거절 완료");
    }

}