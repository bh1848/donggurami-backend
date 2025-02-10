package com.USWCicrcleLink.server.aplict.repository;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public class AplictRepositoryCustomImpl implements AplictRepositoryCustom {

    @PersistenceContext
    private final EntityManager em;

    @Override
    @Transactional
    public void deleteByClubClubId(Long clubId) {
        String jpql = "DELETE FROM Aplict a WHERE a.club.clubId = :clubId";
        em.createQuery(jpql)
                .setParameter("clubId", clubId)
                .executeUpdate();
    }

    // 동아리 지원자 조회
    @Override
    public List<Aplict> findAllWithProfileByClubId(Long clubId, boolean checked) {
        return em.createQuery(
                        "SELECT ap FROM Aplict ap JOIN FETCH ap.profile p" +
                                " WHERE ap.club.id = :clubId AND ap.checked = :checked",
                        Aplict.class
                ).setParameter("clubId", clubId)
                .setParameter("checked", checked)
                .getResultList();
    }

    // 불합격자 동아리 지원자 조회
    @Override
    public List<Aplict> findAllWithProfileByClubIdAndFailed(Long clubId, boolean checked, AplictStatus status) {
        return em.createQuery(
                        "SELECT ap FROM Aplict ap JOIN FETCH ap.profile p" +
                                " WHERE ap.club.id = :clubId AND ap.checked = :checked AND ap.aplictStatus = :status",
                        Aplict.class
                ).setParameter("clubId", clubId)
                .setParameter("checked", checked)
                .setParameter("status", status)
                .getResultList();
    }

}
