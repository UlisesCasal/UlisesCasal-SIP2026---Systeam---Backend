package com.systeam.backend.auth.dto;
import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse{
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private Long userId;
    private String email;
    private Set<String> roles;
    private Set<String> permissions; 
}