package com.mylog.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.exception.CMissingDataException;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @InjectMocks
    private S3Service s3Service;

    @Mock
    private S3Client s3Client;

    private MockMultipartFile file;
    private String bucketName = "test-bucket";
    private String region = "ap-northeast-2";
    private String fileName = "test.jpg";
    private String contentType = "image/jpeg";

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        // 테스트용 객체 초기화
        file = new MockMultipartFile("file", fileName, contentType, "test image content".getBytes());

        // @Value 모킹
        var bucketNameField = S3Service.class.getDeclaredField("bucketName");
        bucketNameField.setAccessible(true);
        bucketNameField.set(s3Service, bucketName);
    }


    @Test
    void 이미지_업로드_성공_URL반환() throws IOException {
        // Given
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(PutObjectResponse.builder().build());
        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);

        // When
        Optional<String> result = s3Service.upload(file);

        // Then
        assertThat(result).isPresent();
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest actualRequest = requestCaptor.getValue();
        assertThat(actualRequest.key().substring(37)).isEqualTo( fileName);
        assertThat(actualRequest.bucket()).isEqualTo(bucketName);
        assertThat(actualRequest.contentType()).isEqualTo(contentType);
    }

    @Test
    void 이미지_업로드_실패_예외반환(){
        //given
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenThrow(RuntimeException.class);

        //when
        assertThatThrownBy(() -> s3Service.upload(file))
            .isInstanceOf(CMissingDataException.class)
            .hasMessage("s3 이미지 업로드에 실패했습니다.");

    }




    @Test
    void 이미지_삭제_성공_삭제완료() {
        // Given
        String url = "https://mylog-imgsource.s3.ap-northeast-2.amazonaws.com/f47ac10b-58cc-4372-a567-0e02b2c3d479_test.jpg";
        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(mock(
            DeleteObjectResponse.class));

        // When
        s3Service.deleteImage(url);

        // Then
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }
}