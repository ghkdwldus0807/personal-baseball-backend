package com.personal_baseball.oauth2.controller;

import com.personal_baseball.oauth2.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
@Slf4j
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/reissueToken")
    public ResponseEntity<?> reissueAccessToken(HttpServletRequest request) {

        //Authorization Header에서 access Token 추출
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Access Token이 제공되지 않았습니다.");
        }

        String accessToKen = authorizationHeader.substring(7);

        //Access Token 재발급
        String newAccessToken = tokenService.reissueAccessTokenUsingAccessToken(accessToKen);

        //Authorization Header에 새로 발급된 Access Token을 담아 전달
        return ResponseEntity.ok().header(HttpHeaders.AUTHORIZATION,"Bearer "+newAccessToken).build();

    }

}
