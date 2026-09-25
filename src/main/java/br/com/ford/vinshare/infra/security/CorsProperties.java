package br.com.ford.vinshare.infra.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/** Origens autorizadas a consumir a API pelo navegador (ex.: app Expo Web, dashboard). */
@ConfigurationProperties(prefix = "api.security.cors")
public record CorsProperties(List<String> allowedOrigins) {}
