<<<<<<<< HEAD:src/main/java/com/mylog/common/security/ExceptionHandlerFilter.java
package com.mylog.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylog.common.exception.BusinessException;
import com.mylog.common.response.ErrorResponse;
import com.mylog.exception.common.CUnAuthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (BusinessException e) {
            ErrorResponse errorResponse = ErrorResponse.from(e.getCode());

            response.setStatus(e.getCode().getStatus());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }
}
