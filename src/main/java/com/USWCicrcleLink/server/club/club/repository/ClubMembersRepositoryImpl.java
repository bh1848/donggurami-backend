package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.domain.ClubMembers;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class ClubMembersRepositoryImpl implements ClubMembersRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<ClubMembers> findAllWithProfile(Long clubId) {
        return em.createQuery(
                        "select cm from ClubMembers cm" +
                                " join fetch cm.profile p" +
                                " where cm.club.clubId = :clubId",
                        ClubMembers.class
                ).setParameter("clubId", clubId)
                .getResultList();
    }

    @Override
    @Transactional
    public List<Long> findClubIdsByUserId(Long userId) {
        return em.createQuery(
                        "select cm.club.clubId from ClubMembers cm" +
                                " where cm.profile.user.userId = :userId",
                        Long.class
                ).setParameter("userId", userId)
                .getResultList();
    }
}
