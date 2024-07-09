package com.USWCicrcleLink.server.aplict.dto;

import com.USWCicrcleLink.server.aplict.domain.Aplict;
import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import com.USWCicrcleLink.server.profile.domain.Profile;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AplictResponse {
    private Long id;
    private Profile profile;
    private String aplictGoogleFormUrl;
    private LocalDateTime submittedAt;
    private AplictStatus status;

    public static AplictResponse from(Aplict aplict) {
        return new AplictResponse(
                aplict.getId(),
                aplict.getProfile(),
                aplict.getAplictGoogleFormUrl(),
                aplict.getSubmittedAt(),
                aplict.getAplictStatus()
        );
    }
}
