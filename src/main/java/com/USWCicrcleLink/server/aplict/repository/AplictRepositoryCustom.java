package com.USWCicrcleLink.server.aplict.repository;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AplictRepositoryCustom {
    void deleteByClubClubId(Long clubId);

    Page<Aplict> findAllWithProfileByClubId(Long clubId, Pageable pageable, boolean checked);

    Page<Aplict> findAllWithProfileByClubIdAndFailed(Long clubId, Pageable pageable, boolean checked, AplictStatus status);
}
