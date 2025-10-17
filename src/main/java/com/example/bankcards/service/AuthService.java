package com.example.bankcards.service;

import com.example.bankcards.dto.response.AuthStatusResponse;
import com.example.bankcards.dto.request.UserLoginRequest;
import com.example.bankcards.dto.request.UserRegistrationRequest;

public interface AuthService {

    AuthStatusResponse signUp(UserRegistrationRequest request);

    AuthStatusResponse signIn(UserLoginRequest request);

}
