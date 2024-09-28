package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.admin.admin.dto.ClubAdminListResponse;
import com.USWCicrcleLink.server.global.util.s3File.Service.S3FileUploadService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
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
    public List<ClubAdminListResponse> findAllWithMemberAndLeaderCount() {
        String jpql = "SELECT new com.USWCicrcleLink.server.admin.admin.dto.ClubListResponse(c.clubId, c.department, c.clubName, c.leaderName, " +
                "(COUNT(cm) + (CASE WHEN l IS NOT NULL THEN 1 ELSE 0 END))) " +
                "FROM Club c " +
                "LEFT JOIN ClubMembers cm ON c.clubId = cm.club.clubId " +
                "LEFT JOIN Leader l ON c.clubId = l.club.clubId " +
                "GROUP BY c.clubId, l.leaderId";

        TypedQuery<ClubAdminListResponse> query = em.createQuery(jpql, ClubAdminListResponse.class);
        return query.getResultList();
    }

    @Override
    public void deleteClubAndDependencies(Long clubId) {

        // 1. Club과 관련된 참조 엔티티들 삭제
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

        // 모든 키를 합친 리스트
        List<String> s3Keys = new ArrayList<>();
        s3Keys.addAll(clubIntroPhotoKeys);
        s3Keys.addAll(clubMainPhotoKeys);

        if (!s3Keys.isEmpty()) {
            s3Keys.forEach(s3FileUploadService::deleteFile);
        }

        // 3. 마지막으로 Club 삭제
        em.createQuery("DELETE FROM Club c WHERE c.clubId = :clubId")
                .setParameter("clubId", clubId)
                .executeUpdate();
    }
}