package com.example.airplanning.configuration;

import com.example.airplanning.filter.NicknameFilter;
import com.example.airplanning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final UserService userService;

    @Bean
    public FilterRegistrationBean addNicknameFilter() {
        FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();

        // 닉네임 필터 등록 => 로그인했는데 닉네임이 없으면(소셜 로그인) 닉네임 등록창으로 이동
        filterFilterRegistrationBean.setFilter(new NicknameFilter(userService));
        filterFilterRegistrationBean.setOrder(1);

        // 모든 URL에 적용
        filterFilterRegistrationBean.addUrlPatterns("/*");
        return filterFilterRegistrationBean;
    }
}
