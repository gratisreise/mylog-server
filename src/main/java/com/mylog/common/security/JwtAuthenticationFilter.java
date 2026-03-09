package com.mylog.common.security;

import com.mylog.common.CommonValue;
import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtProvider tokenProvider;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String accessToken = resolveToken(request);

    if (StringUtils.hasText(accessToken) && tokenProvider.validateAccessToken(accessToken)) {
      Long memberId = tokenProvider.getMemberId(accessToken);
      CustomUserDetails userDetails = new CustomUserDetails(memberId);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    int start = CommonValue.AUTH_PREFIX.length();
    if (isTokenEmpty(bearerToken, start)) {
      throw new BusinessException(ErrorCode.TOKEN_EMPTY);
    }
    return bearerToken.substring(start);
  }

  private static boolean isTokenEmpty(String bearerToken, int start) {
    return !StringUtils.hasText(bearerToken) || !StringUtils.hasText(bearerToken.substring(start));
  }
}
