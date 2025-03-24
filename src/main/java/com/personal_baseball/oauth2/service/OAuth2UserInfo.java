package com.personal_baseball.oauth2.service;

public interface OAuth2UserInfo {

    String getPlatformId();
    String getPlatformType();
    String getEmail();
    String getUserName();

}
