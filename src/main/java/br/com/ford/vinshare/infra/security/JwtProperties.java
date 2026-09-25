package br.com.ford.vinshare.infra.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/** Configurações do JWT (prefixo api.security.jwt em application.properties). */
@ConfigurationProperties(prefix = "api.security.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        String audience,
        Duration accessTokenExpiration,
        Duration refreshTokenExpiration
) {}
