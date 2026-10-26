package com.sews.reporting.dto;

import com.sews.reporting.entity.Role;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String email;
    private String fullName;
    private String department;
    private String phoneNumber;
    private String position;
    private Role role;
}
