package com.personal_baseball.oauth2.service;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class GoogleUserInfo implements OAuth2UserInfo{

    private Map<String,Object> attributes;

    @Override
    public String getPlatformId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getPlatformType() {
        return "google";
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getUserName() {
        return (String) attributes.get("name");
    }
}
