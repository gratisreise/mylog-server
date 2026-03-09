package com.mylog.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonValue {
  public static final String BASIC_CATEGORY = "없음";
  public static final String AUTH_PREFIX = "Bearer ";
  public static final String BASIC_MEMBER_IMAGE =
      "https://mylog-imgsource.s3.ap-northeast-2.amazonaws.com/5162d5b3-266b-4aae-bc16-d7f10fc4b2f1_basic.png";
  public static final int CATEGORY_LIMIT = 20;
}
