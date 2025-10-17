package com.example.bankcards.dto.mapper;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;

public class UserMapper {

    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .roles(RoleMapper.toDto(user.getRoles()))
                .build();
    }

}
