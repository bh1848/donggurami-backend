package com.USWCicrcleLink.server.aplict.repository;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import com.USWCicrcleLink.server.profile.domain.Profile;
import org.checkerframework.checker.units.qual.A;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AplictRepository extends JpaRepository<Aplict, Long> ,AplictRepositoryCustom{
    List<Aplict> findByProfileProfileId(Long profileId);

    Optional<Aplict> findByClub_ClubIdAndIdAndChecked (Long clubId, Long aplictId, boolean checked);

    List<Aplict> findByClub_ClubIdAndChecked (Long clubId, boolean checked);

    Optional<Aplict> findByClub_ClubIdAndIdAndCheckedAndAplictStatus (Long clubId, Long aplictId, boolean checked, AplictStatus status);

    List<Aplict> findAllByDeleteDateBefore(LocalDateTime dateTime);

    void deleteAllByProfile(Profile profile);
}
