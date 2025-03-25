package com.personal_baseball.oauth2.controller;

import com.personal_baseball.oauth2.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
@Slf4j
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/reissueToken")
    public ResponseEntity<?> reissueAccessToken(@RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");

        //refreshToken 재발급
        String newAccessToken = tokenService.reissueAccessToken(refreshToken);

        //Frontend에게 새로운 액세스 토큰 반환
        Map<String,String> response = new HashMap<>();
        response.put("accessToken",newAccessToken);

        return ResponseEntity.ok(response);

    }

}
