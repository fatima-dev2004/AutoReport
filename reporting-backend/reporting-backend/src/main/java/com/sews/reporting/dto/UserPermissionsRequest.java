package com.sews.reporting.dto;

import com.sews.reporting.entity.Permission;
import lombok.Data;

import java.util.Set;

@Data
public class UserPermissionsRequest {
    private Set<Permission> permissions;
}
