package com.legitify.auth_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.legitify.auth_service.dto.UserRequestDto;
import com.legitify.auth_service.dto.UserResponseDto;
import com.legitify.auth_service.entity.User;

@Mapper
public interface UserMapper {

    UserMapper MAPPER = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "authResponseDto", ignore = true)
    UserResponseDto maptoUserResponseDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    User maptoUser(UserRequestDto userRequestDto);
}
