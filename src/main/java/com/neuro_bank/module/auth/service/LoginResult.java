package com.neuro_bank.module.auth.service;

import com.neuro_bank.module.user.dto.response.UserResponse;

public record LoginResult(String accessToken, String refreshToken, UserResponse response){}

