package com.i2i.AuthServer.controller;

import com.i2i.AuthServer.authentication.ClientAuthentication;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/oauth2")
@Slf4j
public class TokenIntrospectionController {

    @Autowired
    private ClientAuthentication clientAuthentication;

    @PostMapping("/custom-introspect")
    public ResponseEntity<Map<String,Object>> introspect(HttpServletRequest request,
                                                         @RequestParam("token") String token) {
        Map<String,Object> response=clientAuthentication.authenticateClient(request,token);
        if(response.size()==1)
        {
            return new ResponseEntity<>(response,HttpStatus.UNAUTHORIZED);
        }
        return  ResponseEntity.ok(response);
    }



}
