package com.mylog.external.s3;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@RequiredArgsConstructor
public class S3Service {

  private final S3Client s3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  private static final String REGION = "ap-northeast-2";

  // 이미지 업로드
  public String upload(MultipartFile file) {

    if (file == null || file.isEmpty()) return null;
    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
    String contentType = file.getContentType();

    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .contentType(contentType)
            .build();

    try {
      s3Client.putObject(
          putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    } catch (Exception e) {
      throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, REGION, fileName);
  }

  // 이미지 삭제
  @Async("threadPoolTaskExecutor")
  public void deleteImage(String url) {
    String fileKey = url.substring(url.lastIndexOf("/") + 1);
    DeleteObjectRequest deleteObjectRequest =
        DeleteObjectRequest.builder().bucket(bucketName).key(fileKey).build();

    s3Client.deleteObject(deleteObjectRequest);
  }
}
