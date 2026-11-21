package com.emedic.management.config;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.time.Instant;

@Configuration
public class SecurityConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfiguration.class);

    private final ObjectMapper objectMapper;

    public SecurityConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(Environment environment) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(java.util.Arrays.stream(environment
                        .getProperty("emedic.web.allowed-origins", "http://localhost:5173").split(","))
                .map(String::trim).filter(StringUtils::hasText).toList());
        cors.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "Accept"));
        cors.setExposedHeaders(java.util.List.of("Location"));
        cors.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cors);
        return source;
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http, Environment environment) throws Exception {
        boolean jwtConfigured = StringUtils.hasText(environment.getProperty("emedic.security.jwt.jwk-set-uri"));
        LOGGER.info("JWT resource server is {}", jwtConfigured ? "enabled" : "disabled (JWK Set URI was not loaded)");
        http.csrf(csrf -> csrf.disable())
                .cors(org.springframework.security.config.Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) ->
                                writeAuthenticationError(request, response, exception, jwtConfigured))
                        .accessDeniedHandler((request, response, exception) ->
                                writeAccessDeniedError(request, response)));
        if (jwtConfigured) {
            http.authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/public/booking/doctors",
                                    "/api/public/booking/doctors/*/availability").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/public/booking/appointments").permitAll()
                            .requestMatchers("/api/consultations/**", "/api/prescriptions/**")
                                .hasAnyRole("CLINICIAN", "ADMIN")
                            .requestMatchers("/api/patients/*/history")
                                .hasAnyRole("CLINICIAN", "ADMIN")
                            .requestMatchers("/api/patients/**")
                                .hasAnyRole("CLINICIAN", "RECEPTIONIST", "ADMIN")
                            .requestMatchers("/api/clinic-workspace/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.POST, "/api/doctors/me").hasRole("CLINICIAN")
                            .requestMatchers("/api/appointments/**")
                                .hasAnyRole("CLINICIAN", "RECEPTIONIST", "ADMIN")
                            .requestMatchers(HttpMethod.GET, "/api/doctors/**", "/api/schedules/**", "/api/medicines/**")
                                .hasAnyRole("CLINICIAN", "RECEPTIONIST", "ADMIN")
                            .requestMatchers("/api/doctors/**", "/api/schedules/**", "/api/medicines/**")
                                .hasRole("ADMIN")
                            .anyRequest().denyAll())
                    .oauth2ResourceServer(oauth -> oauth
                            .authenticationEntryPoint((request, response, exception) ->
                                    writeAuthenticationError(request, response, exception, jwtConfigured))
                            .accessDeniedHandler((request, response, exception) ->
                                    writeAccessDeniedError(request, response))
                            .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                    jwtAuthenticationConverter(environment.getProperty("emedic.security.jwt.roles-claim", "roles")))));
        } else {
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/public/booking/doctors",
                            "/api/public/booking/doctors/*/availability").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/public/booking/appointments").permitAll()
                    .anyRequest().denyAll());
        }
        return http.build();
    }

    @Bean
    @ConditionalOnExpression("'${emedic.security.jwt.jwk-set-uri:}' != ''")
    JwtDecoder jwtDecoder(org.springframework.core.env.Environment environment) {
        String uri = environment.getRequiredProperty("emedic.security.jwt.jwk-set-uri");
        String issuer = environment.getRequiredProperty("emedic.security.jwt.issuer-uri");
        String audience = environment.getRequiredProperty("emedic.security.jwt.audience");
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(uri).build();
        OAuth2TokenValidator<Jwt> audienceValidator = token -> token.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Required audience is missing", null));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer), audienceValidator));
        return decoder;
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter(String rolesClaim) {
        JwtGrantedAuthoritiesConverter scopes = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<org.springframework.security.core.GrantedAuthority> authorities = new ArrayList<>(scopes.convert(jwt));
            Object claim = jwt.getClaims().get(rolesClaim);
            if (claim instanceof Collection<?> roles) {
                roles.stream().filter(String.class::isInstance).map(String.class::cast)
                        .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                        .map(SimpleGrantedAuthority::new).forEach(authorities::add);
            }
            return authorities;
        });
        return converter;
    }

    private void writeAccessDeniedError(HttpServletRequest request, HttpServletResponse response)
            throws java.io.IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var authorities = authentication == null ? java.util.List.<String>of()
                : authentication.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority).toList();
        LOGGER.warn("Authorization denied: method={} grantedAuthorities={}", request.getMethod(), authorities);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new AccessDeniedError(Instant.now(), 403,
                "Forbidden", "You are not permitted to access this resource", request.getRequestURI(), authorities));
    }

    private void writeAuthenticationError(HttpServletRequest request, HttpServletResponse response,
                                          org.springframework.security.core.AuthenticationException exception,
                                          boolean jwtConfigured)
            throws java.io.IOException {
        String authorization = request.getHeader("Authorization");
        boolean bearerTokenPresent = authorization != null && authorization.startsWith("Bearer ")
                && authorization.length() > "Bearer ".length();
        String failureType = exception.getClass().getSimpleName();
        LOGGER.warn("Authentication denied: method={} bearerTokenPresent={} failureType={}",
                request.getMethod(), bearerTokenPresent, failureType);
        String message = !jwtConfigured
                ? "Backend JWT validation is disabled. The JWK Set URI was not loaded when Spring started; check the backend .env location and restart from the management folder."
                : bearerTokenPresent
                ? "The API received a bearer token but rejected it. Check issuer, audience, signing algorithm, and JWK settings."
                : "The API request did not include a bearer access token. Check the frontend token request and API proxy.";
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new AuthenticationError(Instant.now(), 401,
                "Unauthorized", message, request.getRequestURI(), bearerTokenPresent, failureType, jwtConfigured));
    }

    private record AccessDeniedError(Instant timestamp, int status, String error, String message,
                                     String path, java.util.List<String> grantedAuthorities) { }

    private record AuthenticationError(Instant timestamp, int status, String error, String message,
                                       String path, boolean bearerTokenPresent, String failureType,
                                       boolean jwtResourceServerEnabled) { }
}
