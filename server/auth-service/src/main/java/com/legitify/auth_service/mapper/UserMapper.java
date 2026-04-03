package com.legitify.auth_service.mapper;

import com.legitify.auth_service.dto.AuthResponseDto;
import com.legitify.auth_service.dto.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.legitify.auth_service.entity.User;

@Mapper
public interface UserMapper {

    UserMapper MAPPER = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "id", target = "userId")
    UserResponseDto toUserResponseDto(User user);

    AuthResponseDto toAuthResponseDto(
            User user,
            String accessToken
    );
}
