package com.personal_baseball.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

//Access Token 및 Refresh Token 발급
@Slf4j
@Component
public class JWTUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.access-token.expiration-time}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration-time}")
    private long refreshTokenExpiration;

    private SecretKey getSigningKey(){
        byte[] keyBytes = Decoders.BASE64.decode(this.SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //Access Token 발급
    public String generateAccessToken(Long userId){
        log.debug("Access Token이 발급되었습니다. userId : {}",userId);

        return Jwts.builder()
                .claim("userId",userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(this.getSigningKey())
                .compact();
    }

    //Refresh Token 발급
    public String generateRefreshToken(Long userId){
        log.debug("Refresh Token이 발급되었습니다. userId : {}",userId);

        return Jwts.builder()
                .claim("userId",userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(this.getSigningKey())
                .compact();
    }

    //Authorization Header에서 Token 추출
    public String getTokenFromHeader(String authorizationHeader){
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")){
            throw new JwtException("유효하지 않은 Authorization Header입니다.");
        }

        //"Bearer " 이후의 토큰 값 반환
        return authorizationHeader.substring(7);
    }

    //Token에서 UserId 추출
    public Long getUserIdFromToken(String token){
        try{
            return Jwts.parser()
                    .verifyWith(this.getSigningKey()) //서명 검증
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("userId",Long.class);
        } catch (JwtException | IllegalArgumentException e){
            log.warn("유효하지 않은 토큰입니다. (getUserIdFromToken)");
            throw new JwtException("유효하지 않은 JWT 토큰입니다.");
        }
    }


    //Token의 유효기간 확인
    public boolean isTokenExpired(String token){
        try{
            Date expirationDate = Jwts.parser()
                    .verifyWith(this.getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();
            return expirationDate.before(new Date());
        } catch (JwtException | IllegalArgumentException e ){
            log.warn("유효하지 않은 토큰입니다. (isTokenExpired) ");
            throw new JwtException("유효하지 않은 JWT 토큰입니다.");
        }
    }














}
