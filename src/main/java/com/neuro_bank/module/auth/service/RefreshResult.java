package com.neuro_bank.module.auth.service;

public record RefreshResult(String newAccessToken, String newRefreshToken) {
}
