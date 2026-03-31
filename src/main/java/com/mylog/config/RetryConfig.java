package com.mylog.config;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Configuration
public class RetryConfig {

  @Bean
  public RetryTemplate aiRetryTemplate() {
    RetryTemplate template = new RetryTemplate();

    ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
    backOff.setInitialInterval(1000L);
    backOff.setMultiplier(2.0);
    backOff.setMaxInterval(5000L);

    template.setBackOffPolicy(backOff);
    template.setRetryPolicy(
        new SimpleRetryPolicy() {
          @Override
          public boolean canRetry(RetryContext context) {
            if (!super.canRetry(context)) return false;
            Throwable t = context.getLastThrowable();
            if (t instanceof BusinessException ex) {
              return ex.getCode() == ErrorCode.EXTERNAL_API_ERROR;
            }
            return false;
          }
        });

    return template;
  }

  @Bean
  public RetryTemplate s3RetryTemplate() {
    RetryTemplate template = new RetryTemplate();

    ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
    backOff.setInitialInterval(500L);
    backOff.setMultiplier(2.0);
    backOff.setMaxInterval(3000L);

    template.setBackOffPolicy(backOff);
    template.setRetryPolicy(
        new SimpleRetryPolicy() {
          @Override
          public boolean canRetry(RetryContext context) {
            if (!super.canRetry(context)) return false;
            Throwable t = context.getLastThrowable();
            return t instanceof S3Exception;
          }
        });

    return template;
  }
}
