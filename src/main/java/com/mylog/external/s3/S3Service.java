<<<<<<<< HEAD:src/main/java/com/mylog/external/s3/S3Provider.java
<<<<<<<< HEAD:src/main/java/com/mylog/external/s3/S3Provider.java
package com.mylog.external.s3;

========
package com.mylog.s3;

========
package com.mylog.s3;

>>>>>>>> df0a55de6d27f9fdc5dd1d7257f9e30801976b60:infra/src/main/java/com/mylog/s3/S3Service.java

import com.mylog.exception.ErrorCode;
import com.mylog.exception.common.CMissingDataException;
import com.mylog.exception.common.CommonError;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@RequiredArgsConstructor
public class S3Provider {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    //이미지 업로드
    public String upload(MultipartFile file)  {

        if(file == null || file.isEmpty()) return null;
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String contentType = file.getContentType();
        String region = "ap-northeast-2";

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .contentType(contentType)
            .build();

<<<<<<<< HEAD:src/main/java/com/mylog/external/s3/S3Provider.java
========
        try{
            s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (RuntimeException | IOException e) {
            throw new CMissingDataException(CommonError.FAILED_IMAGE_UPLOAD);
        }
>>>>>>>> origin/main:infra/src/main/java/com/mylog/s3/S3Service.java

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
            file.getInputStream(), file.getSize()
        ));


        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
    }

    // 이미지 삭제
    @Async
    public void deleteImage(String url) {
        String fileKey = url.substring(url.lastIndexOf("/") + 1);
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(fileKey)
            .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

}
