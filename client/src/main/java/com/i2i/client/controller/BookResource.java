package com.i2i.client.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BookResource {

    @GetMapping("/books")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
    public ResponseEntity<String> getBooks(Authentication authentication) {

        String username = authentication.getName();

        return ResponseEntity.ok("Hi " + username + ", here are some books [book1, book2],  ");
    }
}
