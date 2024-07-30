package com.USWCicrcleLink.server.aplict.repository;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
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
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Aplict> resultList = query.getResultList();

        long total = getAplictTotalCount(clubId);

        return new PageImpl<>(resultList, pageable, total);
    }

    private long getAplictTotalCount(Long clubId) {
        String countJpql = "SELECT COUNT(ap) FROM Aplict ap WHERE ap.club.id = :clubId";
        return em.createQuery(countJpql, Long.class).setParameter("clubId", clubId).getSingleResult();
    }
}
