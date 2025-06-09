package com.i2i.AuthServer.controller;

import com.i2i.AuthServer.dto.UserDto;
import com.i2i.AuthServer.dto.UserResponseDto;
import com.i2i.AuthServer.model.User;
import com.i2i.AuthServer.service.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users/V1")
@Slf4j
public class UserController {

    private final UserServiceImpl userService;


    public UserController(UserServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserDto userDTO) {
        UserResponseDto createdUser = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }


    @PutMapping("/{userName}")
    public ResponseEntity<UserResponseDto> updateUser(Authentication authentication,@PathVariable String userName, @Valid @RequestBody UserDto userDTO) {

        UserResponseDto updatedUser = userService.updateUser(userName,userDTO,authentication.getName());
        return ResponseEntity.ok(updatedUser);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/{id}/disable")
    public ResponseEntity<UserResponseDto> disableUser(Authentication authentication,@PathVariable Long id) {
        UserResponseDto disabledUser = userService.disableUser(id,authentication.getName());
        return ResponseEntity.ok(disabledUser);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
