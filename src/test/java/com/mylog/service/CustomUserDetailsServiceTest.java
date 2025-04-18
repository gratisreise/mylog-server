package com.mylog.service;

import static org.junit.jupiter.api.Assertions.*;

import com.mylog.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @InjectMocks
    CustomUserDetailsService customUserDetailsService;

    @Mock
    MemberRepository memberRepository;

    @BeforeEach
    void setUp() {

    }
}