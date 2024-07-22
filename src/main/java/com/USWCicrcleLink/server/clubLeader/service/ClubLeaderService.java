package com.USWCicrcleLink.server.clubLeader.service;

import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.ClubIntro;
import com.USWCicrcleLink.server.club.domain.ClubMembers;
import com.USWCicrcleLink.server.club.domain.RecruitmentStatus;
import com.USWCicrcleLink.server.club.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.club.repository.ClubMembersRepositoryImpl;
import com.USWCicrcleLink.server.club.repository.ClubRepository;
import com.USWCicrcleLink.server.clubLeader.domain.Leader;
import com.USWCicrcleLink.server.clubLeader.dto.*;
import com.USWCicrcleLink.server.clubLeader.repository.LeaderRepository;
import com.USWCicrcleLink.server.global.response.ApiResponse;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private final ProfileRepository profileRepository;

    // 대표 사진 경로
    @Value("${file.mainPhoto-dir}")
    private String mainPhotoDir;
    @Value("${file.introPhoto-dir}")
    private String introPhotoDir;
    @Value("#{'${file.allowed-extensions}'.split(',')}")
    private List<String> allowedExtensions;

    // 동아리 기본 정보 변경
    public void updateClubInfo(ClubInfoRequest clubInfoRequest) throws IOException {

        // 토큰 적용, 예외 처리 시 변경
        Leader leader = leaderRepository.findByLeaderUUID(clubInfoRequest.getLeaderUUID())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 회장이 아닙니다."));
        log.info("동아리 회장 조회 결과: {}", leader);

        // 동아리 조회
        Club club = clubRepository.findById(leader.getClub().getClubId())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 아닙니다."));
        log.info("동아리 조회 결과: {}", club);

        // 사진 파일 업로드 과정
        createMainPhotoDir();// 사진 파일 디렉터리 없는 경우 생성

        // 파일 저장 경로
        String mainPhotoPath = saveFile(clubInfoRequest.getMainPhoto(), club.getMainPhotoPath(), mainPhotoDir);

        // 동아리 정보 변경
        club.updateClubInfo(mainPhotoPath, clubInfoRequest.getChatRoomURL(),
                clubInfoRequest.getKatalkID(), clubInfoRequest.getClubInsta());

        clubRepository.save(club);
        log.info("동아리 기본 정보 변경 완료: {}", club.getClubName());
    }

    // 동아리 소개 변경
    public void updateClubIntro(ClubIntroRequest clubIntroRequest) throws IOException {

        // 토큰 적용, 예외 처리 시 변경
        Leader leader = leaderRepository.findByLeaderUUID(clubIntroRequest.getLeaderUUID())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 회장이 아닙니다."));
        log.info("동아리 회장 조회 결과: {}", leader);

        // 동아리 조회
        Club club = clubRepository.findById(leader.getClub().getClubId())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 아닙니다."));
        log.info("동아리 조회 결과: {}", club);

        // 사진 파일 업로드 과정
        createIntroPhotoDir();// 사진 파일 디렉터리 없는 경우 생성

        // 기존 파일 경로가 있는지 확인
        ClubIntro existingClubIntro = clubIntroRepository.findByClub(club)
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 소개가 아닙니다."));

        // 파일 있나 ? 덮어쓰기 : 비워두기
        String introPhotoPath = saveFile(clubIntroRequest.getIntroPhoto(),
                existingClubIntro.getClubIntroPhotoPath(), introPhotoDir);

        String additionalPhotoPath1 = saveFile(clubIntroRequest.getAdditionalPhoto1(),
                existingClubIntro.getAdditionalPhotoPath1(), introPhotoDir);

        String additionalPhotoPath2 = saveFile(clubIntroRequest.getAdditionalPhoto2(),
                existingClubIntro.getAdditionalPhotoPath2(), introPhotoDir);

        String additionalPhotoPath3 = saveFile(clubIntroRequest.getAdditionalPhoto3(),
                existingClubIntro.getAdditionalPhotoPath3(), introPhotoDir);

        String additionalPhotoPath4 = saveFile(clubIntroRequest.getAdditionalPhoto4(),
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

    // 사진 파일 저장 후 파일 경로 리턴
    private String saveFile(MultipartFile file, String existingFilePath, String photoDir) throws IOException {

        // 파일이 없거나 넣지 않은 경우
        if (file == null || file.isEmpty()) {
            return existingFilePath;
        }

        // 기존 파일 삭제
        if (existingFilePath != null) {
            deleteFile(Path.of(existingFilePath));
        }

        // 파일 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        // 지원하는 확장자인지 검증
        validateFileExtension(extension);

        // UUID 이용해서 파일 이름 생성
        String uniqueFileName = UUID.randomUUID() + "_" + originalFilename;
        String filePath = Paths.get(photoDir, uniqueFileName).toString();

        // 파일 저장
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IOException("파일 저장 중 오류가 발생했습니다.", e);
        }

        return filePath;
    }

    // 기존 파일 삭제
    private void deleteFile(Path existingFilePath) throws IOException {
        if (Files.exists(existingFilePath)) {
            try {
                Files.delete(existingFilePath);
            } catch (IOException e) {
                throw new IOException("기존 파일 삭제 중 오류가 발생했습니다.", e);
            }
        }
    }

    // 파일 확장자 추출
    private String getFileExtension(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("파일 이름이 유효하지 않습니다.");
        }
        int dotIndex = filename.lastIndexOf('.');

        // 없으면 예외 처리, 있으면 . 이후에 확장자를 추출
        if (dotIndex == -1) {
            throw new IllegalArgumentException("파일 확장자가 없습니다.");
        }

        return filename.substring(dotIndex + 1).toLowerCase();
    }

    // 지원하는 확장자인지 검증
    private void validateFileExtension(String extension) throws IOException {
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new IOException("지원하지 않는 파일 확장자입니다.: " + extension);
        }
    }

    // 동아리 모집 상태 변경
    public RecruitmentStatus toggleRecruitmentStatus(RecruitmentRequest recruitmentRequest) {

        // 원래는 GET 요청임 토큰때문

        // 토큰 적용, 예외 처리 시 변경
        Leader leader = leaderRepository.findByLeaderUUID(recruitmentRequest.getLeaderUUID())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 회장이 아닙니다."));
        log.info("동아리 회장 조회 결과: {}", leader);

        // 동아리 조회
        Club club = clubRepository.findById(leader.getClub().getClubId())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 아닙니다."));
        log.info("동아리 조회 결과: {}", club);

        ClubIntro clubIntro = clubIntroRepository.findByClub(club)
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 소개가 아닙니다."));
        log.info("동아리 소개 조회 결과: {}", clubIntro);

        // 모집 상태 현재와 반전
        club.toggleRecruitmentStatus();

        return clubIntro.getRecruitmentStatus();
    }

    // 소속 동아리원 조회
    public ApiResponse<List<ClubMembersResponse>> findClubMembers(UUID leaderUUID) {
        // 토큰 적용, 예외 처리 시 변경
        Leader leader = leaderRepository.findByLeaderUUID(leaderUUID)
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 회장이 아닙니다."));
        log.info("동아리 회장 조회 결과: {}", leader);

        // 소속 동아리 조회
        Club club = clubRepository.findById(leader.getClub().getClubId())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 아닙니다."));
        log.info("동아리 조회 결과: {}", club);

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
    public ApiResponse deleteClubMember(Long clubMemberId, UUID leaderUUID) {
        // 토큰 적용, 예외 처리 시 변경
        Leader leader = leaderRepository.findByLeaderUUID(leaderUUID)
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 회장이 아닙니다."));
        log.info("동아리 회장 조회 결과: {}", leader);

        // 동아리 조회
        Club club = clubRepository.findById(leader.getClub().getClubId())
                .orElseThrow(() -> new IllegalArgumentException("유효한 동아리 아닙니다."));
        log.info("동아리 조회 결과: {}", club);

        // 동아리원 삭제
        clubMembersRepository.deleteById(clubMemberId);
        return new ApiResponse<>("동아리원 삭제 완료");
    }

}