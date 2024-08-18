package com.USWCicrcleLink.server.club.club.repository;

import com.USWCicrcleLink.server.admin.admin.dto.ClubListResponse;
import com.USWCicrcleLink.server.global.util.s3File.Service.S3FileUploadService;
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

    private final S3FileUploadService s3FileUploadService;

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
        // S3 키를 한 번에 조회하여 S3 파일 삭제
        List<String> s3Keys = em.createQuery(
                        "SELECT cmp.clubMainPhotoS3Key " +
                                "FROM ClubMainPhoto cmp WHERE cmp.club.clubId = :clubId " +
                                "UNION ALL " +
                                "SELECT cip.clubIntroPhotoS3Key " +
                                "FROM ClubIntroPhoto cip WHERE cip.clubIntro.club.clubId = :clubId", String.class)
                .setParameter("clubId", clubId)
                .getResultList();

        // S3 파일 삭제
        for (String key : s3Keys) {
            s3FileUploadService.deleteFile(key);
        }

        // 종속 엔티티와 Club 삭제를 위한 쿼리
        em.createQuery("DELETE FROM ClubIntroPhoto cip WHERE cip.clubIntro.club.clubId = :clubId")
                .setParameter("clubId", clubId)
                .executeUpdate();

        em.createQuery("DELETE FROM ClubMainPhoto cmp WHERE cmp.club.clubId = :clubId")
                .setParameter("clubId", clubId)
                .executeUpdate();

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