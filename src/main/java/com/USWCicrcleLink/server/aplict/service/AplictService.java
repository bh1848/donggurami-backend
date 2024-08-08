package com.USWCicrcleLink.server.aplict.service;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import com.USWCicrcleLink.server.aplict.dto.AplictRequest;
import com.USWCicrcleLink.server.aplict.dto.AplictResponse;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.club.club.repository.ClubRepository;
import com.USWCicrcleLink.server.club.clubIntro.domain.ClubIntro;
import com.USWCicrcleLink.server.club.clubIntro.repository.ClubIntroRepository;
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

    //지원서 작성하기(구글 폼 반환)(모바일)
    @Transactional(readOnly = true)
    public String getGoogleFormUrlByClubId(Long clubId) {
        ClubIntro clubIntro = clubIntroRepository.findByClubClubId(clubId).orElseThrow(() ->
                new NoSuchElementException("해당 동아리에 대한 소개를 찾을 수 없습니다.")
        );

        String googleFormUrl = clubIntro.getGoogleFormUrl();
        if (googleFormUrl == null || googleFormUrl.isEmpty()) {
            throw new NoSuchElementException("구글 폼 URL을 찾을 수 없습니다.");
        }
        return googleFormUrl;
    }

    //동아리 지원서 제출(모바일)
    public void submitAplict(Long clubId, AplictRequest request) {
        // SecurityContextHolder에서 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.user();

        Profile profile = profileRepository.findByUser_UserUUID(user.getUserUUID())
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new NoSuchElementException("동아리를 찾을 수 없습니다."));

        Aplict aplict = Aplict.builder()
                .profile(profile)
                .club(club)
                .aplictGoogleFormUrl(request.getAplictGoogleFormUrl())
                .submittedAt(LocalDateTime.now())
                .aplictStatus(AplictStatus.WAIT)
                .build();

        Aplict savedAplict = aplictRepository.save(aplict);
        AplictResponse.from(savedAplict);
    }
}