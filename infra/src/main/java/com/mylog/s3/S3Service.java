package com.mylog.s3;


import com.mylog.exception.CMissingDataException;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    //이미지 업로드
    public String upload(MultipartFile file)  {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String contentType = file.getContentType();
        String region = "ap-northeast-2";

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .contentType(contentType)
            .build();


        try{
            s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (RuntimeException | IOException e) {
            throw new CMissingDataException("s3 이미지 업로드에 실패했습니다.");
        }

        String result = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
        log.info("s3 result: {}", result);
        return result;
    }

    // 이미지 삭제
    public void deleteImage(String url) {
        String fileKey = url.substring(url.lastIndexOf("/") + 1);
        log.info("fileKey: {}", fileKey);
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(fileKey)
            .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

}
