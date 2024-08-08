package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.admin.admin.dto.ClubListResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional
public class ClubRepositoryCustomImpl implements ClubRepositoryCustom {

    @PersistenceContext
    private final EntityManager em;

    @Override
    public List<ClubListResponse> findAllWithMemberAndLeaderCount() {
        String jpql = "SELECT new com.USWCicrcleLink.server.admin.admin.dto.ClubListResponse(c.clubId, c.department, c.clubName, c.leaderName, COUNT(cm)) " +
                "FROM Club c LEFT JOIN ClubMembers cm ON c.clubId = cm.club.clubId " +
                "GROUP BY c.clubId";

        TypedQuery<ClubListResponse> query = em.createQuery(jpql, ClubListResponse.class);
        return query.getResultList();
    }

    @Override
    public void deleteClubAndDependencies(Long clubId) {
        em.createQuery("DELETE FROM Aplict a WHERE a.club.clubId = :clubId")
                .setParameter("clubId", clubId)
                .executeUpdate();
        em.createQuery("DELETE FROM ClubIntro ci WHERE ci.club.clubId = :clubId")
                .setParameter("clubId", clubId)
                .executeUpdate();
        em.createQuery("DELETE FROM ClubMembers cm WHERE cm.club.clubId = :clubId")
                .setParameter("clubId", clubId)
                .executeUpdate();
        em.createQuery("DELETE FROM Leader l WHERE l.club.clubId = :clubId")
                .setParameter("clubId", clubId)
                .executeUpdate();
        em.createQuery("DELETE FROM Club c WHERE c.clubId = :clubId")
                .setParameter("clubId", clubId)
                .executeUpdate();
    }
}