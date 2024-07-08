package com.USWCicrcleLink.server.profile.domain.api;

import com.USWCicrcleLink.server.profile.domain.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

}
