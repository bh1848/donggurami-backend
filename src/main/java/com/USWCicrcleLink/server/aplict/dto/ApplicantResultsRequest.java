package com.USWCicrcleLink.server.aplict.dto;


import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import lombok.*;

import java.util.UUID;

@Data
public class ApplicantResultsRequest {

    private UUID aplictUUID;

    private AplictStatus aplictStatus;
}
