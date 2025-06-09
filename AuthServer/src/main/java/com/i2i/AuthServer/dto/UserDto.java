package com.i2i.AuthServer.dto;


import lombok.*;

import jakarta.validation.constraints.*;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    @NotBlank(message = "First name is required")
    @Size(max = 15, message = "First name must be less than 15 characters")
    private String firstName;
    @NotBlank(message = "Last name is required")
    @Size(max = 15, message = "Last name must be less than 15 characters")
    private String lastName;
    @NotBlank(message = "Email is required")
    @Email
    private String email;
    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
    private String username;
    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{5,}$",
            message = "Password must be at least 5 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    private String password;
    private boolean enabled;
    @NotEmpty(message = "At least one role must be assigned")
    private Set<RoleDto> roles;
}
