<<<<<<<< HEAD:src/main/java/com/mylog/common/security/JwtAuthenticationFilter.java
package com.mylog.common.security;

========
package com.mylog.common.filter;

import com.mylog.auth.service.TokenBlackListService;
import com.mylog.response.CommonValue;
import com.mylog.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
<<<<<<<< HEAD:src/main/java/com/mylog/common/security/JwtAuthenticationFilter.java

    private final JwtProvider tokenProvider;
========
    private final JwtUtil tokenProvider;
    private final UserDetailsService userDetailsService;
    private final TokenBlackListService tokenBlackListService;
>>>>>>>> origin/main:api/src/main/java/com/mylog/common/filter/JwtAuthenticationFilter.java

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        String accessToken = resolveToken(request);

<<<<<<<< HEAD:src/main/java/com/mylog/common/security/JwtAuthenticationFilter.java
        if (StringUtils.hasText(accessToken) && tokenProvider.validateAccessToken(accessToken)) {
            Long memberId = tokenProvider.getMemberId(accessToken);
            CustomUserDetails userDetails = new CustomUserDetails(memberId);
========
        if (StringUtils.hasText(jwt) && tokenProvider.validateAccessToken(jwt)) {
            String username = tokenProvider.getUsername(jwt);
            if(!tokenBlackListService.isLogout(username, jwt)){
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                //검증
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
>>>>>>>> origin/main:api/src/main/java/com/mylog/common/filter/JwtAuthenticationFilter.java

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                log.warn("블랙 리스트에 등록된 토큰입니다.");
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
<<<<<<<< HEAD:src/main/java/com/mylog/common/security/JwtAuthenticationFilter.java

        String prefix = "Bearer ";
        int start = prefix.length();
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(prefix)) {
            return bearerToken.substring(start);
========
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(CommonValue.AUTH_PREFIX)) {
            return bearerToken.substring(CommonValue.AUTH_PREFIX_LENGTH);
>>>>>>>> origin/main:api/src/main/java/com/mylog/common/filter/JwtAuthenticationFilter.java
        }
        return null;
    }
}
