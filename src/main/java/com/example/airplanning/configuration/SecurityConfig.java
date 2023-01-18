package com.example.airplanning.configuration;

import com.example.airplanning.exception.AppException;
import com.example.airplanning.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .httpBasic().disable()
                .csrf().disable()
                .cors().and()

                .authorizeRequests()
                .antMatchers("/api/v1/hello").authenticated()
                .anyRequest().permitAll()
                .and()

                // 폼 로그인 시작
                .formLogin()
//                .loginPage(".html") 커스텀한 로그인 페이지 사용 가능. 생략시 스프링에서 제공하는 페이지로 감
//                .defaultSuccessUrl("/home) 로그인 성공 후 이동 페이지. 아래의 successHandler 구현하면 생략해도 됨.
                .failureUrl("/api/login2") //실패 시 이동 페이지
//                .loginProcessingUrl() 로그인 Form Action Url
                .usernameParameter("userName") // html 에서 "userName"란 파라메터 이름을 사용해야 함
                .passwordParameter("password") // html 에서 "password"란 파라메터 이름을 사용해야 함
                .successHandler( // 로그인 성공시, 세션 유지시간 3600초,  리다이렉트는 "http://localhost:8081/api/login"로 설정
                        new AuthenticationSuccessHandler() {
                            @Override
                            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                                HttpSession session = request.getSession();
                                session.setMaxInactiveInterval(3600);
                                response.sendRedirect("/api/login");
                            }
                        }
                )
                .failureHandler( // 로그인 실패 시 리다이렉트 "http://localhost:8081/api/login" 로 설정
                        new AuthenticationFailureHandler() {
                            @Override
                            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                                if(exception instanceof AuthenticationServiceException) {
                                    request.setAttribute("LoginFailMessage", "죄송합니다. 시스템에 오류가 발생했습니다.");
                                } else if(exception instanceof BadCredentialsException) {
                                    request.setAttribute("LoginFailMessage", "아이디 또는 비밀번호가 일치하지 않습니다.");
                                } else if(exception instanceof DisabledException) {
                                    request.setAttribute("LoginFailMessage", "현재 사용할 수 없는 계정입니다.");
                                } else if(exception instanceof LockedException) {
                                    request.setAttribute("LoginFailMessage", "현재 잠긴 계정입니다.");
                                } else if(exception instanceof AccountExpiredException) {
                                    request.setAttribute("LoginFailMessage", "이미 만료된 계정입니다.");
                                } else if(exception instanceof CredentialsExpiredException) {
                                    request.setAttribute("LoginFailMessage", "비밀번호가 만료된 계정입니다.");
                                } else if(exception instanceof UsernameNotFoundException) {
                                    request.setAttribute("LoginFailMessage", "계정을 찾을 수 없습니다.");
                                }

                                request.getRequestDispatcher("/api/login2").forward(request, response);
                            }
                        }
                )
                .permitAll()

                // 로그아웃
                .and()
                .logout()
                .invalidateHttpSession(true)

                .and()
                .build();
    }
}
