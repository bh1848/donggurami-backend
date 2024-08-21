package com.USWCicrcleLink.server.user.dto;

import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyAplictResponse {

    private Long clubId;

    private String mainPhotoPath;

    private String clubName;

    private String leaderName;

    private String leaderHp;

    private String clubInsta;

    private AplictStatus aplictStatus;

}
