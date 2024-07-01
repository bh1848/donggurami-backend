package com.USWCicrcleLink.server.profile.domain.api;

import com.USWCicrcleLink.server.profile.domain.service.ProfileService;
import com.USWCicrcleLink.server.profile.domain.dto.ProfileRequest;
import com.USWCicrcleLink.server.profile.domain.dto.ProfileResponse;
import com.USWCicrcleLink.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("profile")
@RequiredArgsConstructor
public class Controller {

    private final ProfileService profileService;

}
