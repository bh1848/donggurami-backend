package com.USWCicrcleLink.server.aplict.service;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import com.USWCicrcleLink.server.aplict.dto.AplictRequest;
import com.USWCicrcleLink.server.aplict.dto.AplictResponse;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.repository.ClubMembersRepository;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
import com.USWCicrcleLink.server.global.exception.ExceptionType;
import com.USWCicrcleLink.server.global.exception.errortype.BaseException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubException;
import com.USWCicrcleLink.server.global.exception.errortype.ClubIntroException;
import com.USWCicrcleLink.server.global.exception.errortype.UserException;
import com.USWCicrcleLink.server.global.security.util.CustomUserDetails;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import com.USWCicrcleLink.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AplictService {
    private final AplictRepository aplictRepository;
    private final ClubRepository clubRepository;
    private final ProfileRepository profileRepository;
    private final ClubIntroRepository clubIntroRepository;
    private final ClubMembersRepository clubMembersRepository;

    // 동아리 지원 가능 여부 확인
    public void checkIfCanApply(Long clubId) {
        // 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.user();

        log.debug("동아리 지원 가능 여부 확인 요청 - ClubID: {}, UserUUID: {}", clubId, user.getUserUUID());

        // 사용자 프로필 조회
        Profile profile = profileRepository.findByUser_UserUUID(user.getUserUUID())
                .orElseThrow(() -> {
                    log.error("동아리 지원 가능 여부 확인 실패 - 프로필 없음, UserUUID: {}", user.getUserUUID());
                    return new UserException(ExceptionType.USER_NOT_EXISTS);
                });

        // 이미 해당 동아리에 지원했는지 확인
        if (aplictRepository.existsByProfileAndClub_ClubId(profile, clubId)) {
            log.warn("동아리 지원 실패 - 이미 지원한 사용자, ClubID: {}, UserUUID: {}", clubId, user.getUserUUID());
            throw new BaseException(ExceptionType.ALREADY_APPLIED);
        }

        // 이미 동아리 멤버인지 확인
        if (clubMembersRepository.existsByProfileAndClub_ClubId(profile, clubId)) {
            log.warn("동아리 지원 실패 - 이미 동아리 멤버, ClubID: {}, UserUUID: {}", clubId, user.getUserUUID());
            throw new ClubException(ExceptionType.ALREADY_MEMBER);
        }

        // 해당 동아리의 모든 회원 정보 조회
        List<Profile> clubMembers = clubMembersRepository.findProfilesByClubId(clubId);

        // 중복 학번 및 전화번호 확인
        for (Profile member : clubMembers) {
            if (profile.getUserHp().equals(member.getUserHp())) {
                log.warn("동아리 지원 실패 - 중복된 전화번호, ClubID: {}, UserUUID: {}", clubId, user.getUserUUID());
                throw new BaseException(ExceptionType.PHONE_NUMBER_ALREADY_REGISTERED);
            }
            if (profile.getStudentNumber().equals(member.getStudentNumber())) {
                log.warn("동아리 지원 실패 - 중복된 학번, ClubID: {}, UserUUID: {}", clubId, user.getUserUUID());
                throw new BaseException(ExceptionType.STUDENT_NUMBER_ALREADY_REGISTERED);
            }
        }

        log.debug("동아리 지원 가능 - ClubID: {}, UserUUID: {}", clubId, user.getUserUUID());
    }

    //지원서 작성하기(구글 폼 반환)(모바일)
    @Transactional(readOnly = true)
    public String getGoogleFormUrlByClubId(Long clubId) {
        log.debug("구글 폼 URL 조회 요청 - ClubID: {}", clubId);

        ClubIntro clubIntro = clubIntroRepository.findByClubClubId(clubId)
                .orElseThrow(() -> {
                    log.warn("구글 폼 URL 조회 실패 - 클럽 소개 없음, ClubID: {}", clubId);
                    return new ClubIntroException(ExceptionType.CLUB_INTRO_NOT_EXISTS);
                });

        String googleFormUrl = clubIntro.getGoogleFormUrl();
        if (googleFormUrl == null || googleFormUrl.isEmpty()) {
            log.warn("구글 폼 URL 조회 실패 - 구글 폼 없음, ClubID: {}", clubId);
            throw new ClubIntroException(ExceptionType.GOOGLE_FORM_URL_NOT_EXISTS);
        }

        log.debug("구글 폼 URL 조회 성공 - ClubID: {}", clubId);
        return googleFormUrl;
    }

    //동아리 지원서 제출(모바일)
    public void submitAplict(Long clubId, AplictRequest request) {
        // SecurityContextHolder에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.user();

        log.debug("동아리 지원서 제출 요청 - ClubID: {}, UserUUID: {}", clubId, user.getUserUUID());

        Profile profile = profileRepository.findByUser_UserUUID(user.getUserUUID())
                .orElseThrow(() -> {
                    log.error("동아리 지원서 제출 실패 - 프로필 없음, UserUUID: {}", user.getUserUUID());
                    return new UserException(ExceptionType.USER_NOT_EXISTS);
                });

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> {
                    log.error("동아리 지원서 제출 실패 - 존재하지 않는 동아리, ClubID: {}", clubId);
                    return new ClubException(ExceptionType.CLUB_NOT_EXISTS);
                });

        Aplict aplict = Aplict.builder()
                .profile(profile)
                .club(club)
                .aplictGoogleFormUrl(request.getAplictGoogleFormUrl())
                .submittedAt(LocalDateTime.now())
                .aplictStatus(AplictStatus.WAIT)
                .build();

        aplictRepository.save(aplict);
        log.info("동아리 지원서 제출 성공 - ClubID: {}, UserUUID: {}, Status: {}", clubId, user.getUserUUID(), AplictStatus.WAIT);
    }
}