package com.USWCicrcleLink.server.aplict.dto;


import com.USWCicrcleLink.server.aplict.domain.AplictStatus;
import lombok.*;

@Data
public class ApplicantResultsRequest {

    private Long aplictId;

    private AplictStatus aplictStatus;
}
