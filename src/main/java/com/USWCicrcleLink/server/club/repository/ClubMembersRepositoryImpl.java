package com.USWCicrcleLink.server.club.repository;

import com.USWCicrcleLink.server.club.domain.ClubMembers;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

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
}
