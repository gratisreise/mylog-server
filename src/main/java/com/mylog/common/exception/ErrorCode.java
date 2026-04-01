package com.mylog.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
  // ========================================================================
  // AUTH - 인증 관련
  // ========================================================================
  INVALID_CREDENTIALS(401, "AUTH_001", "이메일 또는 비밀번호가 올바르지 않습니다."),
  TOKEN_EXPIRED(401, "AUTH_002", "액세스 토큰이 만료되었습니다."),
  TOKEN_INVALID(401, "AUTH_003", "유효하지 않은 토큰입니다."),
  TOKEN_EMPTY(401, "AUTH_004", "토큰이 비어있습니다."),
  REFRESH_TOKEN_INVALID(401, "AUTH_005", "리프레시 토큰이 유효하지 않습니다."),
  OAUTH_TOKEN_FAILED(401, "AUTH_006", "소셜 로그인 토큰 검증에 실패했습니다."),
  OAUTH_UNSUPPORTED_PROVIDER(400, "AUTH_007", "지원하지 않는 소셜 로그인 제공자입니다."),
  OAUTH_API_FAILED(502, "AUTH_008", "OAuth API 호출에 실패했습니다."),
  OAUTH_USER_INFO_FAILED(502, "AUTH_009", "사용자 정보 조회에 실패했습니다."),
  INVALID_REDIRECT_URI(400, "AUTH_010", "허용되지 않은 리다이렉트 URI입니다."),
  ALREADY_LOGGED_IN(400, "AUTH_011", "이미 로그인된 상태입니다."),
  NOT_LOGGED_IN(401, "AUTH_012", "로그인이 필요한 서비스입니다."),

  // ========================================================================
  // MEMBER - 회원 관련
  // ========================================================================
  MEMBER_NOT_FOUND(404, "MEMBER_001", "사용자를 찾을 수 없습니다."),
  MEMBER_ALREADY_EXISTS(409, "MEMBER_002", "이미 사용 중인 이메일입니다."),
  MEMBER_WITHDRAWN(410, "MEMBER_003", "이미 탈퇴한 회원입니다."),
  MEMBER_NAME_REQUIRED(400, "MEMBER_004", "이름은 필수 입력 항목입니다."),
  MEMBER_PROFILE_IMAGE_INVALID(400, "MEMBER_005", "프로필 이미지 URL이 유효하지 않습니다."),
  MEMBER_DEACTIVATED(403, "MEMBER_006", "비활성화된 회원입니다."),

  // ========================================================================
  // ARTICLE - 게시글 관련
  // ========================================================================
  ARTICLE_NOT_FOUND(404, "ART_001", "게시글을 찾을 수 없습니다."),
  ARTICLE_FORBIDDEN(403, "ART_002", "해당 게시글에 접근할 권한이 없습니다."),
  ARTICLE_TITLE_TOO_LONG(400, "ART_003", "제목은 100자 이내로 입력해야 합니다."),
  ARTICLE_CONTENT_TOO_LONG(400, "ART_004", "내용은 10000자 이내로 입력해야 합니다."),

  // ========================================================================
  // TAG - 게시글 관련
  // ========================================================================
  TAG_NOT_FOUND(404, "TAG_001", "태그를 찾을 수 없습니다."),

  // ========================================================================
  // CATEGORY - 카테고리 관련
  // ========================================================================
  CATEGORY_NOT_FOUND(404, "CAT_001", "카테고리를 찾을 수 없습니다."),
  CATEGORY_LIMIT_REACHED(400, "CAT_002", "카테고리 최대 개수(20개)를 초과했습니다."),
  CATEGORY_FORBIDDEN(403, "CAT_003", "해당 카테고리에 접근할 권한이 없습니다."),
  CATEGORY_NAME_DUPLICATED(409, "CAT_004", "이미 존재하는 카테고리 이름입니다."),

  // ========================================================================
  // COMMENT - 댓글 관련
  // ========================================================================
  COMMENT_NOT_FOUND(404, "CMT_001", "댓글을 찾을 수 없습니다."),
  COMMENT_FORBIDDEN(403, "CMT_002", "해당 댓글에 접근할 권한이 없습니다."),
  COMMENT_ARTICLE_NOT_FOUND(404, "CMT_003", "게시글을 찾을 수 없습니다."),
  COMMENT_CONTENT_TOO_LONG(400, "CMT_004", "댓글은 500자 이내로 입력해야 합니다."),

  // ========================================================================
  // NOTIFICATION - 알림 관련
  // ========================================================================
  NOTIFICATION_NOT_FOUND(404, "NOTI_001", "알림을 찾을 수 없습니다."),
  NOTIFICATION_FCM_TOKEN_INVALID(400, "NOTI_002", "FCM 토큰이 유효하지 않습니다."),
  NOTIFICATION_TIME_INVALID(400, "NOTI_003", "알림 시간 형식이 올바르지 않습니다. (HH:mm 형식)"),
  NOTIFICATION_TYPE_INVALID(400, "NOTI_004", "알림 유형이 유효하지 않습니다."),

  // ========================================================================
  // VALIDATION - 입력 검증
  // ========================================================================
  EMAIL_FORMAT_INVALID(400, "VAL_001", "이메일 형식이 올바르지 않습니다."),
  PASSWORD_TOO_SHORT(400, "VAL_002", "비밀번호는 최소 8자 이상이어야 합니다."),
  PASSWORD_TOO_WEAK(400, "VAL_003", "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."),
  NAME_TOO_LONG(400, "VAL_004", "이름은 50자 이내로 입력해야 합니다."),
  TERMS_AGREEMENT_REQUIRED(400, "VAL_005", "이용약관 동의는 필수 항목입니다."),
  PRIVACY_AGREEMENT_REQUIRED(400, "VAL_006", "개인정보 처리방침 동의는 필수 항목입니다."),
  WITHDRAW_REASON_REQUIRED(400, "VAL_007", "탈퇴 사유는 필수 항목입니다."),
  PAGE_SIZE_EXCEEDED(400, "VAL_008", "페이지 크기는 최대 100까지 가능합니다."),
  PAGE_NUMBER_INVALID(400, "VAL_009", "페이지 번호는 0 이상이어야 합니다."),
  TAG_FORMAT_INVALID(400, "VAL_010", "태그 형식이 올바르지 않습니다."),
  SORT_TYPE_INVALID(400, "VAL_011", "정렬 방식이 올바르지 않습니다."),
  FILE_UPLOAD_FAILED(400, "VAL_012", "파일 업로드에 실패했습니다."),
  REQUEST_BODY_MISSING(400, "VAL_013", "요청 본문이 누락되었습니다."),
  PATH_VARIABLE_MISSING(400, "VAL_014", "필수 경로 변수가 누락되었습니다."),
  QUERY_PARAMETER_MISSING(400, "VAL_015", "필수 쿼리 파라미터가 누락되었습니다."),
  DATE_FORMAT_INVALID(400, "VAL_016", "날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)"),
  DATE_TIME_FORMAT_INVALID(400, "VAL_017", "날짜시간 형식이 올바르지 않습니다. (yyyy-MM-dd'T'HH:mm:ss)"),
  VALIDATION_FAILED(400, "VAL_018", "요청 값 검증에 실패했습니다."),
  TYPE_MISMATCH(400, "VAL_019", "파라미터 타입이 올바르지 않습니다."),

  // ========================================================================
  // SYSTEM - 시스템 관련
  // ========================================================================
  INTERNAL_SERVER_ERROR(500, "SYS_001", "서버 내부 오류가 발생했습니다."),
  DATABASE_ERROR(500, "SYS_002", "데이터베이스 오류가 발생했습니다."),
  EXTERNAL_API_ERROR(502, "SYS_003", "외부 API 호출에 실패했습니다."),
  FILE_STORAGE_ERROR(500, "SYS_004", "파일 저장에 실패했습니다."),
  FILE_STORAGE_ACCESS_DENIED(503, "FILE_001", "파일 저장소 접근이 거부되었습니다."),
  FILE_STORAGE_BUCKET_NOT_FOUND(503, "FILE_002", "S3 버킷을 찾을 수 없습니다."),
  FILE_READ_ERROR(500, "FILE_003", "파일 읽기에 실패했습니다."),
  FILE_EMPTY(400, "FILE_004", "파일이 비어있습니다."),
  FILE_SIZE_EXCEEDED(413, "FILE_005", "파일 크기가 10MB를 초과했습니다."),
  FILE_INVALID_TYPE(400, "FILE_006", "지원하지 않는 파일 형식입니다."),
  RATE_LIMIT_EXCEEDED(429, "SYS_005", "요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요."),
  SERVICE_UNAVAILABLE(503, "SYS_006", "서비스를 일시적으로 사용할 수 없습니다."),

  // ========================================================================
  // CUSTOM STYLE - 커스텀 문체 스타일 관련
  // ========================================================================
  CUSTOM_STYLE_NOT_FOUND(404, "STYLE_001", "커스텀 문체 스타일을 찾을 수 없습니다."),
  CUSTOM_STYLE_FORBIDDEN(403, "STYLE_002", "해당 커스텀 문체 스타일에 접근할 권한이 없습니다."),
  CUSTOM_STYLE_LIMIT_REACHED(400, "STYLE_003", "커스텀 문체 스타일은 최대 3개까지 생성할 수 있습니다."),
  CUSTOM_STYLE_NAME_DUPLICATED(409, "STYLE_004", "이미 존재하는 커스텀 문체 스타일 이름입니다."),

  // ========================================================================
  // SECURITY - 보안 관련
  // ========================================================================
  ACCESS_DENIED(403, "SEC_001", "접근 권한이 없습니다."),
  UNAUTHORIZED_USER(401, "SEC_002", "인증 정보가 없거나 유효하지 않습니다."),

  // ========================================================================
  // 미처리 오류
  // ========================================================================
  UNKNOWN_ERROR(500, "UNKNOWN", "알 수 없는 오류가 발생했습니다.");
  private final int status;
  private final String code;
  private final String message;
}
