package com.USWCicrcleLink.server.aplict.repository;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import com.USWCicrcleLink.server.profile.domain.Profile;
import org.checkerframework.checker.units.qual.A;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AplictRepository extends JpaRepository<Aplict, Long> ,AplictRepositoryCustom{
    List<Aplict> findByProfileProfileId(Long profileId);

    Optional<Aplict> findByAplictUUID(UUID aplictUUID);

    Optional<Aplict> findByClub_ClubIdAndAplictUUIDAndChecked(Long clubId, UUID aplictUUID, boolean checked);

    List<Aplict> findByClub_ClubIdAndChecked (Long clubId, boolean checked);

    Optional<Aplict> findByClub_ClubIdAndAplictUUIDAndCheckedAndAplictStatus(Long clubId, UUID aplictUUID, boolean checked, AplictStatus status);
    List<Aplict> findAllByDeleteDateBefore(LocalDateTime dateTime);

    void deleteAllByProfile(Profile profile);

    // AplictRepository에서 지원 여부 확인
    boolean existsByProfileAndClub_ClubId(Profile profile, Long clubId);
}
