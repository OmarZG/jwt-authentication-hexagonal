package org.zgo.auth.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.zgo.auth.domain.model.RefreshToken;
import org.zgo.auth.infrastructure.persistence.entity.RefreshTokenEntity;

@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {

    RefreshTokenMapper INSTANCE = Mappers.getMapper(RefreshTokenMapper.class);

    // Mapeo simple: los nombres de las propiedades coinciden
    RefreshTokenEntity toEntity(RefreshToken refreshToken);

    RefreshToken toDomain(RefreshTokenEntity refreshTokenEntity);
}