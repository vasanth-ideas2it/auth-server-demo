package com.i2i.AuthServer.dto;

import lombok.*;

import java.util.Set;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private boolean enabled;
    private Set<RoleDto> roles;
}
