package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.admin.admin.dto.AdminClubListResponse;
import com.USWCicrcleLink.server.global.s3File.Service.S3FileUploadService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Transactional
public class ClubRepositoryCustomImpl implements ClubRepositoryCustom {

    @PersistenceContext
    private final EntityManager em;

    private final S3FileUploadService s3FileUploadService;
    @Override
    public Page<AdminClubListResponse> findAllWithMemberAndLeaderCount(Pageable pageable) {
        // c: Club 테이블, cm: ClubMembers 테이블, l: Leader 테이블
        // 각 Club의 멤버 수와 리더 정보를 함께 계산하여 AdminClubListResponse 객체로 반환
        String jpql = "SELECT new com.USWCicrcleLink.server.admin.admin.dto.AdminClubListResponse(c.clubUUID, c.department, c.clubName, c.leaderName, " +
                "(COUNT(cm) + (CASE WHEN l IS NOT NULL THEN 1 ELSE 0 END))) " +
                "FROM Club c " + // Club 테이블을 조회
                "LEFT JOIN ClubMembers cm ON c.clubId = cm.club.clubId " + // ClubMembers 테이블과 LEFT JOIN
                "LEFT JOIN Leader l ON c.clubId = l.club.clubId " + // Leader 테이블과 LEFT JOIN
                "GROUP BY c.clubId, l.leaderId"; // Club ID와 Leader ID로 그룹화

        // Club 테이블에서 전체 Club 개수를 카운트하는 JPQL
        String countJpql = "SELECT COUNT(c) FROM Club c";

        // JPQL을 실행하기 위한 TypedQuery 생성
        TypedQuery<AdminClubListResponse> query = em.createQuery(jpql, AdminClubListResponse.class);
        // 페이지네이션 설정
        query.setFirstResult((int) pageable.getOffset()); // 시작 위치 설정
        query.setMaxResults(pageable.getPageSize()); // 페이지 크기 설정

        // 전체 Club 개수를 조회
        Long totalCount = em.createQuery(countJpql, Long.class).getSingleResult();

        // JPQL 쿼리 결과를 리스트로 변환
        List<AdminClubListResponse> results = query.getResultList();

        // 결과를 Page 객체로 반환
        return new PageImpl<>(results, pageable, totalCount);
    }

    @Override
    public void deleteClubAndDependencies(Long clubId) {

        em.createQuery("DELETE FROM ClubMemberAccountStatus cmas WHERE cmas.club.clubId = :clubId")
                .setParameter("clubId", clubId)
                .executeUpdate();

        em.createQuery("DELETE FROM ClubHashtag ch WHERE ch.club.clubId = :clubId")
                .setParameter("clubId", clubId)
                .executeUpdate();

        em.createQuery("DELETE FROM ClubCategoryMapping cm WHERE cm.club.clubId = :clubId")
                .setParameter("clubId", clubId)
                .executeUpdate();

        em.createQuery("DELETE FROM ClubMembers cm WHERE cm.club.clubId = :clubId")
                .setParameter("clubId", clubId)
                .executeUpdate();

        em.createQuery("DELETE FROM Aplict a WHERE a.club.clubId = :clubId")
                .setParameter("clubId", clubId)
                .executeUpdate();

        em.createQuery("DELETE FROM ClubIntroPhoto cip WHERE cip.clubIntro.club.clubId = :clubId")
                .setParameter("clubId", clubId)
                .executeUpdate();

        em.createQuery("DELETE FROM ClubMainPhoto cmp WHERE cmp.club.clubId = :clubId")
                .setParameter("clubId", clubId)
                .executeUpdate();

        em.createQuery("DELETE FROM ClubIntro ci WHERE ci.club.clubId = :clubId")
                .setParameter("clubId", clubId)
                .executeUpdate();

        em.createQuery("DELETE FROM Leader l WHERE l.club.clubId = :clubId")
                .setParameter("clubId", clubId)
                .executeUpdate();

        // 2. S3에서 동아리와 관련된 모든 사진 파일 삭제
        List<String> clubIntroPhotoKeys = em.createQuery(
                        "SELECT cip.clubIntroPhotoS3Key FROM ClubIntroPhoto cip WHERE cip.clubIntro.club.clubId = :clubId", String.class)
                .setParameter("clubId", clubId)
                .getResultList();

        List<String> clubMainPhotoKeys = em.createQuery(
                        "SELECT cmp.clubMainPhotoS3Key FROM ClubMainPhoto cmp WHERE cmp.club.clubId = :clubId", String.class)
                .setParameter("clubId", clubId)
                .getResultList();

        List<String> s3Keys = new ArrayList<>();
        s3Keys.addAll(clubIntroPhotoKeys);
        s3Keys.addAll(clubMainPhotoKeys);

        if (!s3Keys.isEmpty()) {
            s3FileUploadService.deleteFiles(s3Keys);
        }

        em.createQuery("DELETE FROM Club c WHERE c.clubId = :clubId")
                .setParameter("clubId", clubId)
                .executeUpdate();
    }
}