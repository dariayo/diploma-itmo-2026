package com.diploma.dto;

import lombok.Data;
import com.diploma.enums.Role;

@Data
public class SignUpRequest {
    private String username;
    private String email;
    private String phone;
    private String fullName;
    private Role role;
    private String password;
}

