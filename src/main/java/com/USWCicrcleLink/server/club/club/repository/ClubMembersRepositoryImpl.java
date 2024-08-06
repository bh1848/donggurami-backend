package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.club.club.domain.ClubMembers;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

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
    public Page<ClubMembers> findAllWithProfileByClubId(Long clubId, Pageable pageable) {
        /*
            쿼리 생성 fetch Join
            ClubMembers + Profile
        */
        String jpql = "SELECT cm FROM ClubMembers cm JOIN FETCH cm.profile p" +
                " WHERE cm.club.id = :clubId";

        TypedQuery<ClubMembers> query = em.createQuery(jpql, ClubMembers.class);

        query.setParameter("clubId", clubId);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(query.getResultList(), pageable, getClubMembersTotalCount(clubId));
    }

    private long getClubMembersTotalCount(Long clubId) {
        String countJpql = "SELECT COUNT(cm) FROM ClubMembers cm WHERE cm.club.id = :clubId";
        return em.createQuery(countJpql, Long.class).setParameter("clubId", clubId).getSingleResult();
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
