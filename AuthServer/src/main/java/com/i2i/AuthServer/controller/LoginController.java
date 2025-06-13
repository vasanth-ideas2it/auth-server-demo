package com.i2i.AuthServer.controller;

import com.i2i.AuthServer.authentication.UserAuthentication;
import com.i2i.AuthServer.exceptionHandling.AuthenticationException;
import com.i2i.AuthServer.service.RefreshTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/oauth")
@Slf4j
public class LoginController {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserAuthentication userAuthentication;

    @PostMapping("/token")
    public ResponseEntity<?> getToken(@RequestParam String username,
                                      @RequestParam String password) {
        try {
            //Generate Access token
            String accessToken=userAuthentication.authenticateAndGetToken(username, password);

            //Generate Refresh token and store in database
            String refreshToken = refreshTokenService.createRefreshToken(username);

            return ResponseEntity.ok(Map.of(
                    "access_token", accessToken,
                    "refresh_token", refreshToken,
                    "token_type", "Bearer"
            ));

        } catch (BadCredentialsException ex) {
            throw new AuthenticationException( "Invalid credentials");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestParam String refreshToken) {
        String accessToken=refreshTokenService.refreshAndGetAccessToken(refreshToken);
        return ResponseEntity.ok(Map.of(
                "access_token", accessToken,
                "token_type", "Bearer"
        ));
    }

}
