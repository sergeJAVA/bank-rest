package com.example.bankcards.dto.mapper;

import com.example.bankcards.dto.RoleDto;
import com.example.bankcards.entity.Role;

import java.util.HashSet;
import java.util.Set;

public class RoleMapper {

    public static Set<RoleDto> toDto(Set<Role> roles) {
        Set<RoleDto> roleDtos = new HashSet<>();
        for (Role role : roles) {
            RoleDto roleDto = RoleDto.builder()
                    .name(role.getName())
                    .build();
            roleDtos.add(roleDto);
        }
        return roleDtos;
    }

}
