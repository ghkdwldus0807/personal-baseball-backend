package com.personal_baseball.security;


import com.personal_baseball.domain.RefreshToken;
import com.personal_baseball.domain.User;
import com.personal_baseball.oauth2.service.GoogleUserInfo;
import com.personal_baseball.oauth2.service.OAuth2UserInfo;
import com.personal_baseball.user.repository.RefreshTokenRepository;
import com.personal_baseball.user.repository.UserRepository;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${jwt.redirect}")
    private String REDIRECT_URI;

    @Value("${jwt.access-token.expiration-time}")
    private long ACCESS_TOKEN_EXPIRATION_TIME;

    @Value("${jwt.refresh-token.expiration-time}")
    private long REFRESH_TOKEN_EXPIRATION_TIME;

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException{

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = authToken.getAuthorizedClientRegistrationId();

        OAuth2UserInfo oAuth2UserInfo = null;
        if("google".equalsIgnoreCase(registrationId)){
            log.info("Google Login이 요청되었습니다.");
            oAuth2UserInfo = new GoogleUserInfo(authToken.getPrincipal().getAttributes());
        }

        if(oAuth2UserInfo == null){
            throw new IllegalArgumentException("OAuth2 로그인 요청 오류 : 지원하지 않는 Oauth Provider입니다");
        }

        //User 정보를 Oauth Provider에게서 가져오기
        String platformId = oAuth2UserInfo.getPlatformId();
        String name = oAuth2UserInfo.getUserName();
        String email = oAuth2UserInfo.getEmail();

        //DB에서 유저 조회
        Optional<User> existUser = userRepository.findByEmailAndPlatformType(email,registrationId);
        User user;

        if(existUser.isEmpty()){
            //신규 유저인 경우 -> 회원가입
            log.info("신규 유저 입니다. 회원가입을 진행합니다.");

            user = User.builder()
                    .platformType(registrationId)
                    .platformId(platformId)
                    .email(email)
                    .userName(name)
                    .build();
            userRepository.save(user);
        }
        else{
            //기존 유저일 경우
            log.info("기존 유저 입니다. 로그인 처리 합니다.");
            user = existUser.get();

            //기존 유저의 남아있던 리프레시 토큰 삭제
            refreshTokenRepository.deleteByUserId(user.getUserId());
        }


        //새로운 리프레시 토큰 재발급
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setUserId(user.getUserId());
        newRefreshToken.setToken(refreshToken);
        newRefreshToken.setCreatedAt(Instant.now());
        newRefreshToken.setExpiryDate(Instant.now().plusMillis(REFRESH_TOKEN_EXPIRATION_TIME));

        refreshTokenRepository.save(newRefreshToken);

        //ACCESS 토큰 발급
        String accessToken = jwtUtil.generateAccessToken(user.getUserId());

        //프론트엔드로 리다이렉트
        String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
        String redirectUri = String.format(REDIRECT_URI, encodedName,accessToken, refreshToken);

        log.info("리다이렉트 URI : {}", redirectUri);
        getRedirectStrategy().sendRedirect(request,response,redirectUri);

    }


}
