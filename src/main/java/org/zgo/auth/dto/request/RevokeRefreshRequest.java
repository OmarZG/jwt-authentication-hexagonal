package org.zgo.auth.dto.request;

public class RevokeRefreshRequest {
    private String refreshToken;

    public RevokeRefreshRequest() {
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}