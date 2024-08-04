package com.USWCicrcleLink.server.aplict.domain;

import com.USWCicrcleLink.server.club.club.domain.Club;
import com.USWCicrcleLink.server.profile.domain.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "APLICT_TABLE")
public class Aplict {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aplict_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Column(name = "aplict_google_form_url", nullable = false)
    private String aplictGoogleFormUrl;

    @Column(name = "aplict_submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "aplict_status", nullable = false)
    private AplictStatus aplictStatus = AplictStatus.WAIT;

    @Column(name = "checked")
    private boolean checked;

    @Column(name = "delete_date")
    private LocalDateTime deleteDate;

    public void updateAplictStatus(AplictStatus newStatus, boolean checked, LocalDateTime deleteDate) {
        this.aplictStatus = newStatus;
        this.checked = checked;
        this.deleteDate = deleteDate;
    }

    public void updateFailedAplictStatus(AplictStatus newStatus) {
        this.aplictStatus = newStatus;
    }
}