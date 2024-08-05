//package com.USWCicrcleLink.server.club.club.repository;
//
//import com.USWCicrcleLink.server.admin.admin.dto.ClubListResponse;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.PersistenceContext;
//import jakarta.persistence.TypedQuery;
//import lombok.RequiredArgsConstructor;
//
//import java.util.List;
//
//@RequiredArgsConstructor
//public class ClubRepositoryCustomImpl implements ClubRepositoryCustom {
//
//    @PersistenceContext
//    private final EntityManager em;
//
//    @Override
//    public List<ClubListResponse> findAllWithMemberAndLeaderCount() {
//        String jpql = "SELECT new com.USWCicrcleLink.server.admin.admin.dto.ClubListResponse(c.clubId, c.department, c.clubName, c.leaderName, COUNT(cm)) " +
//                "FROM Club c LEFT JOIN ClubMembers cm ON c.clubId = cm.club.clubId " +
//                "GROUP BY c.clubId";
//
//        TypedQuery<ClubListResponse> query = em.createQuery(jpql, ClubListResponse.class);
//        return query.getResultList();
//    }
//}