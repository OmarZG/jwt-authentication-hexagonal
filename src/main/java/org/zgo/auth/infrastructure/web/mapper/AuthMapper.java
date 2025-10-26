package org.zgo.auth.infrastructure.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.zgo.auth.application.port.in.AuthResult;
import org.zgo.auth.application.port.in.LoginParameters;
import org.zgo.auth.application.port.in.RegisterUserData;
import org.zgo.auth.application.port.in.UserResult;
import org.zgo.auth.domain.model.User;
import org.zgo.auth.infrastructure.web.dto.request.LoginRequest;
import org.zgo.auth.infrastructure.web.dto.request.RegisterRequest;
import org.zgo.auth.infrastructure.web.dto.response.AuthenticationResponse;
import org.zgo.auth.infrastructure.web.dto.response.UserResponse;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    RegisterUserData toRegisterUserData(RegisterRequest request);

    LoginParameters toLoginParameters(LoginRequest request);

    AuthenticationResponse toAuthenticationResponse(AuthResult result);

    UserResponse toUserResponse(UserResult result);

    @Mapping(target = "roles", expression = "java(user.getRoles().stream().map(Enum::name).toList())")
    UserResult toUserResult(User user);
}