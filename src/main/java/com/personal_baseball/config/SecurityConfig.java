package com.personal_baseball.config;


import com.personal_baseball.security.OAuthLoginFailureHandler;
import com.personal_baseball.security.OAuthLoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OAuthLoginSuccessHandler oAuthLoginSuccessHandler;
    private final OAuthLoginFailureHandler oAuthLoginFailureHandler;

    //CORS 설정
    CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration corsConfiguration = new CorsConfiguration();
            corsConfiguration.setAllowedHeaders(Collections.singletonList("*"));
            corsConfiguration.setAllowedMethods(Collections.singletonList("*"));
            corsConfiguration.setAllowedOriginPatterns(Collections.singletonList("*"));
            corsConfiguration.setAllowCredentials(true);
            return corsConfiguration;
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception{
        httpSecurity.httpBasic(HttpBasicConfigurer::disable) //HTTP 기본 인증 비활성화 (JWT 기반 인증방식을 사용하고 있기 때문)
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource())) //cors 설정
                .csrf(AbstractHttpConfigurer :: disable)
                .authorizeHttpRequests(authorize -> authorize.requestMatchers("/**").permitAll()) //요청 인가 설정
                .oauth2Login(oauth -> oauth.successHandler(oAuthLoginSuccessHandler) //Oauth 로그인 설정, 성공 실패 핸들러 추가
                                                                            .failureHandler(oAuthLoginFailureHandler));

        return httpSecurity.build();


    }




}
