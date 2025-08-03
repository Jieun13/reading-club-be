package com.readingclub.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class AuthDto {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
        private UserDto.Response user;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime expiresAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshTokenRequest {
        private String refreshToken;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KakaoUserInfo {
        private Long id;
        private KakaoAccount kakao_account;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class KakaoAccount {
            private Profile profile;
            
            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Profile {
                private String nickname;
                private String profile_image_url;
            }
        }
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KakaoTokenResponse {
        private String access_token;
        private String token_type;
        private String refresh_token;
        private Integer expires_in;
        private String scope;
        private Integer refresh_token_expires_in;
    }
}
