package com.USWCicrcleLink.server.club.service;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.dto.AplictResponse;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.club.domain.Club;
import com.USWCicrcleLink.server.club.domain.Department;
import com.USWCicrcleLink.server.club.domain.Leader;
import com.USWCicrcleLink.server.club.dto.ClubByDepartmentResponse;
import com.USWCicrcleLink.server.club.dto.ClubInfoRequest;
import com.USWCicrcleLink.server.club.dto.ClubResponse;
import com.USWCicrcleLink.server.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.repository.LeaderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ClubService {
    private final ClubRepository clubRepository;
    private final AplictRepository aplictRepository;
    private final LeaderRepository leaderRepository;

    // 대표 사진 경로
    @Value("${file.mainPhoto-dir}")
    private String mainPhtoDir;
    @Value("#{'${file.allowed-extensions}'.split(',')}")
    private List<String> allowedExtensions;

    //모든 동아리 조회
    @Transactional(readOnly = true)
    public List<ClubResponse> getAllClubs() {
        log.info("모든 동아리 조회");
        List<Club> clubs = clubRepository.findAll();
        if (clubs.isEmpty()) {
            throw new NoSuchElementException("동아리가 없습니다.");
        }
        return clubs.stream().map(ClubResponse::new).collect(Collectors.toList());
    }

    //동아리 조회
    @Transactional(readOnly = true)
    public ClubResponse getClubById(Long id) {
        log.info("동아리 조회 id: {}", id);
        Club club = clubRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 ID를 가진 동아리를 찾을 수 없습니다.")
        );
        return new ClubResponse(club);
    }

    //분과별 동아리 조회
    @Transactional(readOnly = true)
    public List<ClubByDepartmentResponse> getClubsByDepartment(Department department) {
        log.info("분과별 동아리 조회: {}", department);
        List<Club> clubs = clubRepository.findByDepartment(department);
        if (clubs.isEmpty()) {
            throw new NoSuchElementException("해당 분과에 속하는 동아리가 없습니다.");
        }
        return clubs.stream()
                .map(ClubByDepartmentResponse::new)
                .collect(Collectors.toList());
    }

    //해당동아리 지원서 조회
    public List<AplictResponse> getAplictByClubId(Long clubId) {
        List<Aplict> aplicts = aplictRepository.findByClub(clubRepository.findById(clubId).orElseThrow(() -> new IllegalArgumentException("동아리를 찾을 수 없습니다.")));
        return aplicts.stream()
                .map(AplictResponse::from)
                .collect(Collectors.toList());
    }

    // 동아리 기본 정보 뱐걍
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
        createUploadDir();// 사진 파일 디렉터리 없는 경우 생성

        // 파일 저장 경로
        String mainPhotoPath = saveFile(clubInfoRequest.getMainPhoto(), club.getMainPhotoPath());

        // 동아리 정보 변경
        club.updateClubInfo(mainPhotoPath, clubInfoRequest.getChatRoomURL(),
                clubInfoRequest.getKatalkID(), clubInfoRequest.getClubInsta());

        clubRepository.save(club);
        log.info("동아리 기본 정보 변경 완료: {}", club.getClubName());
    }

    // 사진 파일 저장 디렉터리 없는 경우 생성
    private void createUploadDir() throws IOException {
        Files.createDirectories(Paths.get(mainPhtoDir));
    }

    // 사진 파일 저장 후 파일 경로 리턴
    private String saveFile(MultipartFile file, String existingFilePath) throws IOException {

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
        String filePath = Paths.get(mainPhtoDir, uniqueFileName).toString();
        Files.copy(file.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
        return filePath;
    }

    // 기존 파일 삭제
    private void deleteFile(Path existingFilePath) throws IOException {
        if (Files.exists(existingFilePath)) {
            Files.delete(existingFilePath);
        }
    }

    // 파일 확장자 추출
    private String getFileExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int dotIndex = filename.lastIndexOf('.');

        // 없으면 빈 값으로 있으면 .이후에 확장자를 추출
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1);
    }

    // 지원하는 확장자인지 검증
    private void validateFileExtension(String extension) throws IOException {
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new IOException("지원하지 않는 파일 확장자입니다.: " + extension);
        }
    }
}