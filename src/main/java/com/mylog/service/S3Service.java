package com.mylog.service;

import com.mylog.config.S3Config;
import com.mylog.controller.TestRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    //이미지 업로드
    public void upload(MultipartFile file){

        try {

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(file.getOriginalFilename())
                .build();

            s3Client.putObject(
                putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
            log.info("File uploaded successfully: " + file.getOriginalFilename());
        } catch (Exception e) {
            log.error("File upload failed: " + file.getOriginalFilename());
            log.error(e.getMessage());
        }
    }

    //이미지 삭제

    //해당 url 이미지 수정

}
