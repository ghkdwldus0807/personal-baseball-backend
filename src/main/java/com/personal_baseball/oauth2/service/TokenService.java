package com.personal_baseball.oauth2.service;


import com.personal_baseball.domain.RefreshToken;
import com.personal_baseball.security.JWTUtil;
import com.personal_baseball.user.repository.RefreshTokenRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public String reissueAccessToken(String refreshToken){

        if(refreshToken == null || refreshToken.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "REFRESH TOKEN이 제공되지 않았습니다.");
        }

        //Refresh Token 유효성 검사
        Long userId;

        try{
            userId = jwtUtil.getUserIdFromToken(refreshToken);
        } catch (JwtException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 refresh token입니다.");
        }

        //DB에서 Refresh Token 찾기
        Optional<RefreshToken> savedRefreshToken = refreshTokenRepository.findByUserId(userId);

        //DB에 Refresh Token이 없을 때
        if(savedRefreshToken.isEmpty() || !savedRefreshToken.get().getToken().equals(refreshToken)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 Refresh Token입니다.");
        }

        //RefreshToken의 유효기간이 지났을 경우
        if(savedRefreshToken.get().getExpiryDate().isBefore(Instant.now())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token이 만료되었습니다.");
        }

        //새로운 액세스 토큰이 발급되었습니다.
        String newAccessToken = jwtUtil.generateAccessToken(userId);
        log.info("새로운 액세스 토큰이 발급되었습니다. userId : {}",userId);

        return newAccessToken;

    }

}
