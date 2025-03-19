package com.mylog.service;


import com.mylog.repository.SocialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocialService {
    private final SocialRepository socialRepository;

}
