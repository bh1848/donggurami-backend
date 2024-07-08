package com.USWCicrcleLink.server.profile.domain.service;

import com.USWCicrcleLink.server.profile.domain.Profile;
import com.USWCicrcleLink.server.profile.domain.repository.ProfileRepository;
import com.USWCicrcleLink.server.profile.domain.dto.ProfileRequest;
import com.USWCicrcleLink.server.profile.domain.dto.ProfileResponse;
import com.USWCicrcleLink.server.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;


}
