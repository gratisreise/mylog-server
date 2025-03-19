package com.mylog.service;


import com.mylog.repository.ArticleTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleTagService {
    private final ArticleTagRepository articleTagRepository;
}
