package com.personal_baseball.security;

import io.jsonwebtoken.Claims;
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
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(this.getSigningKey())
                .compact();
    }

    //Refresh Token 발급
    public String generateRefreshToken(Long userId){
        log.debug("Refresh Token이 발급되었습니다. userId : {}",userId);

        return Jwts.builder()
                .claim("userId",userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(this.getSigningKey())
                .compact();
    }



    //Token에서 UserId 추출
    public Long getUserIdFromToken(String token){
        try{
            //토큰이 유효한 경우
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(this.getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("userId", Long.class);
        }
        catch (io.jsonwebtoken.ExpiredJwtException e){
            //만료된 토큰의 경우
            log.debug("만료된 토큰입니다. claims에서 userId를 추출합니다.");
            return e.getClaims().get("userId",Long.class);
        }

        catch (JwtException | IllegalArgumentException e){
            log.warn("유효하지 않은 토큰입니다. (getUserIdFromToken)");
            throw new JwtException("유효하지 않은 JWT 토큰입니다.");
        }
    }


    //Authorization Header에서 Token 추출
    public String getTokenFromHeader(String authorizationHeader){
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")){
            throw new JwtException("유효하지 않은 Authorization Header입니다.");
        }

        //"Bearer " 이후의 토큰 값 반환
        return authorizationHeader.substring(7);
    }

    //Token의 유효기간 확인
    public boolean isTokenExpired(String token) {
        try {
            Date expirationDate = Jwts.parserBuilder()
                    .setSigningKey(this.getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();

            log.debug("토큰 만료 여부 확인 중, 전달된 토큰: {}", token);
            return expirationDate.before(new Date());

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.debug("Access Token이 만료되었습니다.");
            return true; // 만료된 경우 true 반환

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 토큰입니다. (isTokenExpired)");
            throw new JwtException("유효하지 않은 JWT 토큰입니다.");
        }
    }
















}
