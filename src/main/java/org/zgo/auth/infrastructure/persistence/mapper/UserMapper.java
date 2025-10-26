package org.zgo.auth.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import org.zgo.auth.domain.model.Role;
import org.zgo.auth.domain.model.User;
import org.zgo.auth.infrastructure.persistence.entity.UserEntity;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // Mapeo de Domain Model a JPA Entity
    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRolesToNames")
    UserEntity toEntity(User user);

    // Mapeo de JPA Entity a Domain Model
    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapNamesToRoles")
    User toDomain(UserEntity userEntity);

    // Métodos de conversión personalizada para el Set<Role> <-> Set<String>
    @Named("mapRolesToNames")
    default Set<String> mapRolesToNames(Set<Role> roles) {
        if (roles == null) return null;
        // Convierte el Enum Role a su nombre (String)
        return roles.stream().map(Enum::name).collect(Collectors.toSet());
    }

    @Named("mapNamesToRoles")
    default Set<Role> mapNamesToRoles(Set<String> roleNames) {
        if (roleNames == null) return null;
        // Convierte el String (nombre del rol) al Enum Role
        return roleNames.stream().map(Role::valueOf).collect(Collectors.toSet());
    }
}