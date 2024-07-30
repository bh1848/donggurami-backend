package com.USWCicrcleLink.server.aplict.repository;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    @Override
    public Page<Aplict> findAllWithProfileByClubId(Long clubId, Pageable pageable, boolean checked) {
        String jpql = "SELECT ap FROM Aplict ap JOIN FETCH ap.profile p" +
                " WHERE ap.club.id = :clubId AND ap.checked = :checked";
        TypedQuery<Aplict> query = em.createQuery(jpql, Aplict.class);

        query.setParameter("clubId", clubId);
        query.setParameter("checked", checked);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Aplict> resultList = query.getResultList();

        long total = getAplictTotalCount(clubId, checked);

        return new PageImpl<>(resultList, pageable, total);
    }

    @Override
    public Page<Aplict> findAllWithProfileByClubIdAndFailed(Long clubId, Pageable pageable, boolean checked, AplictStatus status) {
        String jpql = "SELECT ap FROM Aplict ap JOIN FETCH ap.profile p" +
                " WHERE ap.club.id = :clubId AND ap.checked = :checked AND ap.aplictStatus = :status";
        TypedQuery<Aplict> query = em.createQuery(jpql, Aplict.class);

        query.setParameter("clubId", clubId);
        query.setParameter("checked", checked);
        query.setParameter("status", status);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Aplict> resultList = query.getResultList();

        long total = getFailedAplictTotalCount(clubId, checked, status);

        return new PageImpl<>(resultList, pageable, total);
    }

    private long getAplictTotalCount(Long clubId, boolean checked) {
        String countJpql = "SELECT COUNT(ap) FROM Aplict ap" +
                " WHERE ap.club.id = :clubId AND ap.checked = :checked";
        return em.createQuery(countJpql, Long.class)
                .setParameter("clubId", clubId)
                .setParameter("checked",checked)
                .getSingleResult();
    }

    private long getFailedAplictTotalCount(Long clubId, boolean checked, AplictStatus status) {
        String countJpql = "SELECT COUNT(ap) FROM Aplict ap" +
                " WHERE ap.club.id = :clubId AND ap.checked = :checked AND ap.aplictStatus = :status";
        return em.createQuery(countJpql, Long.class)
                .setParameter("clubId", clubId)
                .setParameter("checked", checked)
                .setParameter("status", status)
                .getSingleResult();
    }

}
