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

    // 동아리원과 프로필 조회
    @Override
    public Page<ClubMembers> findAllWithProfileByClubId(Long clubId, Pageable pageable) {
        /*
            쿼리 생성 fetch Join
            ClubMembers + Profile
        */
        // 동아리원 목록을 먼저 가져옴
        List<Long> clubMemberIds = findClubMemberIdsByClubId(clubId, pageable);
        if (clubMemberIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        // 동아리원과 profile을 fetch join
        String jpql = "SELECT cm FROM ClubMembers cm JOIN FETCH cm.profile" +
                " WHERE cm.clubMemberId IN :clubMemberIds";
        TypedQuery<ClubMembers> query = em.createQuery(jpql, ClubMembers.class);
        query.setParameter("clubMemberIds", clubMemberIds);

        List<ClubMembers> resultList = query.getResultList();
        long total = getClubMembersTotalCount(clubId);
        return new PageImpl<>(resultList, pageable, total);
    }

    // 동아리원의 id를 먼저 페이징 처리
    private List<Long> findClubMemberIdsByClubId(Long clubId, Pageable pageable) {
        String jpql = "SELECT cm.clubMemberId FROM ClubMembers cm WHERE cm.club.id = :clubId";
        TypedQuery<Long> query = em.createQuery(jpql, Long.class);
        query.setParameter("clubId", clubId);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        return query.getResultList();
    }

    private long getClubMembersTotalCount(Long clubId) {
        String countJpql = "SELECT COUNT(cm) FROM ClubMembers cm WHERE cm.club.id = :clubId";
        return em.createQuery(countJpql, Long.class).setParameter("clubId", clubId).getSingleResult();
    }

    @Override
    @Transactional
    public void deleteByClubClubId(Long clubId) {
        String jpql = "DELETE FROM ClubMembers cm WHERE cm.club.clubId = :clubId";
        em.createQuery(jpql)
                .setParameter("clubId", clubId)
                .executeUpdate();
    }
}
