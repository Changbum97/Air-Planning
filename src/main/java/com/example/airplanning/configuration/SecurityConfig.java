package com.example.airplanning.configuration;

import com.example.airplanning.configuration.login.CustomOauth2UserService;
import com.example.airplanning.configuration.login.UserDetail;
import com.example.airplanning.domain.enum_class.UserRole;
import com.example.airplanning.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final CustomOauth2UserService customOauth2UserService;
    private final UserService userService;
    private final AccessDeniedHandler accessDeniedHandler;

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf().disable()
                .cors().and()
                .authorizeRequests()
                .antMatchers("/upload").permitAll()
                .antMatchers("/api/order/**").permitAll()
                .antMatchers("/api/v1/hello").authenticated()
                .antMatchers(HttpMethod.GET, "/boards/write").authenticated()
                .antMatchers(HttpMethod.POST, "/api/boards/write").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/boards/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/boards/**").authenticated()
                .antMatchers(HttpMethod.GET, "/users/mypage").authenticated()
//                .antMatchers("/boards/rankup/**").authenticated()
                .antMatchers(HttpMethod.GET, "/reviews/write").authenticated()
                .antMatchers(HttpMethod.POST, "/reviews").authenticated()
                .antMatchers(HttpMethod.GET, "/users/set-nickname").authenticated()
                .antMatchers("/boards/portfolio/write").hasAuthority("PLANNER")
                .antMatchers( "/boards/write").authenticated()
                .antMatchers( HttpMethod.POST,"/boards/**").authenticated()
                .antMatchers(HttpMethod.GET, "/plans/write").authenticated()
                .antMatchers(HttpMethod.POST, "/plans").authenticated()
                .antMatchers("/chat/**").authenticated()
                .anyRequest().permitAll()
                .and()
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler)
                .and()

                // 폼 로그인 시작
                .formLogin()
                .loginPage("/users/login")      // 커스텀한 로그인 페이지 사용 가능. 생략시 스프링에서 제공하는 페이지로 감
                .failureUrl("/api/login2") //실패 시 이동 페이지
                .usernameParameter("userName")  // html 에서 "userName"란 파라메터 이름을 사용해야 함
                .passwordParameter("password")  // html 에서 "password"란 파라메터 이름을 사용해야 함
                .successHandler(                // 로그인 성공시, 세션 유지시간 3600초,  리다이렉트는 홈 화면으로 설정
                        new AuthenticationSuccessHandler() {
                            @Override
                            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                                UserDetail userDetail = (UserDetail) authentication.getPrincipal();
                                if (!userDetail.isAccountNonLocked()) {
                                    throw new LockedException("현재 잠긴 계정입니다.");
                                }

                                HttpSession session = request.getSession();
                                session.setMaxInactiveInterval(3600);
                                response.setContentType("text/html");
                                PrintWriter out = response.getWriter();
                                String loginUserNickname = userService.findUser(authentication.getName()).getNickname();
                                out.println("<script>alert('" + loginUserNickname+ "님 반갑습니다!'); location.href='/';</script>");
                                out.flush();
                            }
                        }
                )
                .failureHandler(
                        new AuthenticationFailureHandler() {
                            @Override
                            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                                String loginFailMessage = "";
                                if(exception instanceof AuthenticationServiceException) {
                                    loginFailMessage = "죄송합니다. 시스템에 오류가 발생했습니다.";
                                } else if(exception instanceof BadCredentialsException || exception instanceof UsernameNotFoundException) {
                                    response.sendRedirect("/users/login?fail");
                                    return;
                                } else if(exception instanceof DisabledException) {
                                    loginFailMessage = "현재 사용할 수 없는 계정입니다.";
                                } else if(exception instanceof LockedException) {
                                    loginFailMessage = "현재 잠긴 계정입니다.";
                                } else if(exception instanceof AccountExpiredException) {
                                    loginFailMessage = "이미 만료된 계정입니다.";
                                } else if(exception instanceof CredentialsExpiredException) {
                                    loginFailMessage = "비밀번호가 만료된 계정입니다.";
                                }

                                response.setContentType("text/html");
                                PrintWriter out = response.getWriter();
                                out.println("<script>alert('" + loginFailMessage + "'); location.href='/users/login';</script>");
                                out.flush();
                            }
                        }
                )
                .permitAll()
                .and()

                // 로그아웃
                .logout()
                .logoutUrl("/users/logout")
                .invalidateHttpSession(true)
                .logoutSuccessHandler(
                        new LogoutSuccessHandler() {
                            @Override
                            public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                                response.setContentType("text/html");
                                PrintWriter out = response.getWriter();
                                out.println("<script>alert('로그아웃 했습니다.'); location.href='/';</script>");
                                out.flush();
                            }
                        }
                )
                .and()

                // OAuth2 소셜 로그인
                .oauth2Login()
                .loginPage("/users/login")
                .failureUrl("/users/login")
                .successHandler(                // 로그인 성공시, 세션 유지시간 3600초,  리다이렉트는 홈 화면으로 설정
                        new AuthenticationSuccessHandler() {
                            @Override
                            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                                UserDetail userDetail = (UserDetail) authentication.getPrincipal();
                                if (!userDetail.isAccountNonLocked()) {
                                    throw new LockedException("현재 잠긴 계정입니다.");
                                }

                                HttpSession session = request.getSession();
                                session.setMaxInactiveInterval(3600);
                                String loginUserNickname = userService.findUser(authentication.getName()).getNickname();
                                if(loginUserNickname != null) {
                                    response.setContentType("text/html");
                                    PrintWriter out = response.getWriter();
                                    out.println("<script>alert('" + loginUserNickname+ "님 반갑습니다!'); location.href='/';</script>");
                                    out.flush();
                                } else {
                                    response.sendRedirect("/");
                                }
                            }
                        }
                )
                .failureHandler(
                        new AuthenticationFailureHandler() {
                            @Override
                            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                                String loginFailMessage = "";
                                if(exception instanceof AuthenticationServiceException) {
                                    loginFailMessage = "죄송합니다. 시스템에 오류가 발생했습니다.";
                                } else if(exception instanceof BadCredentialsException || exception instanceof UsernameNotFoundException) {
                                    response.sendRedirect("/users/login?fail");
                                    return;
                                } else if(exception instanceof DisabledException) {
                                    loginFailMessage = "현재 사용할 수 없는 계정입니다.";
                                } else if(exception instanceof LockedException) {
                                    loginFailMessage = "현재 잠긴 계정입니다.";
                                } else if(exception instanceof AccountExpiredException) {
                                    loginFailMessage = "이미 만료된 계정입니다.";
                                } else if(exception instanceof CredentialsExpiredException) {
                                    loginFailMessage = "비밀번호가 만료된 계정입니다.";
                                }

                                response.setContentType("text/html");
                                PrintWriter out = response.getWriter();
                                out.println("<script>alert('" + loginFailMessage + "'); location.href='/users/login';</script>");
                                out.flush();
                            }
                        })
                .userInfoEndpoint().userService(customOauth2UserService)
                .and()
                .and()
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(true)
                        .sessionRegistry(sessionRegistry())
                        .expiredSessionStrategy(new SessionInformationExpiredStrategy() {
                            @Override
                            public void onExpiredSessionDetected(SessionInformationExpiredEvent event) throws IOException, ServletException {
                                HttpServletResponse response = event.getResponse();
                                response.setContentType("text/html");
                                PrintWriter out = response.getWriter();
                                out.println("<script>alert('계정이 잠금되었습니다'); location.href='/users/login';</script>");
                                out.flush();
                            }
                        })
                )
                .build();
    }
}
