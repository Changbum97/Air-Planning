package com.example.airplanning.filter;

import com.example.airplanning.service.UserService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@AllArgsConstructor
public class NicknameFilter implements Filter {

    private final UserService userService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // 닉네임 설정 페이지, CSS 통과
        String uri = ((HttpServletRequest)request).getRequestURI();
        if(uri.contains("nickname") || uri.contains("css")) {
            chain.doFilter(request, response);
            return;
        }

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 로그인이 안 되어있으면 principal이 String 타입 => 통과
        if(principal instanceof String) {
            chain.doFilter(request, response);
            return;
        }

        // 로그인이 되어있으면 principal이 UserDetails 타입
        UserDetails userDetails = (UserDetails) principal;

        // UserDetails의 UserName으로 User을 찾아와 nickname이 null인지 체크
        // null이라면 nickname 설정 페이지로 이동
        if(userService.findUser(userDetails.getUsername()).getNickname() == null) {
            ((HttpServletResponse) response).sendRedirect("users/set-nickname");
            return;
        }

        chain.doFilter(request, response);
    }
}
