package com.USWCicrcleLink.server.aplict.service;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import com.USWCicrcleLink.server.aplict.dto.AplictRequest;
import com.USWCicrcleLink.server.aplict.dto.AplictResponse;
import com.USWCicrcleLink.server.aplict.repository.AplictRepository;
import com.USWCicrcleLink.server.club.repository.ClubRepository;
import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class AplictService {
    private final AplictRepository aplictRepository;
    private final ClubRepository clubRepository;
    private final ProfileRepository profileRepository;

    public AplictResponse submitAplict(UUID userUUID, Long clubId, AplictRequest request) {
        //userUUID로 Profile 조회
        Profile profile = profileRepository.findByUser_UserUUID(userUUID)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Aplict aplict = Aplict.builder()
                .profile(profile)
                .club(clubRepository.findById(clubId).orElseThrow(() -> new IllegalArgumentException("동아리를 찾을 수 없습니다.")))
                .aplictGoogleFormUrl(request.getAplictGoogleFormUrl())
                .submittedAt(LocalDateTime.now())
                .status(AplictStatus.WAIT)
                .build();

        Aplict savedAplict = aplictRepository.save(aplict);
        return AplictResponse.from(savedAplict);
    }
}