package com.naveen.healthcare.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final String jwtSecret;

    public SecurityConfig(
            @Value("${jwt.secret}") String jwtSecret
    ) {
        this.jwtSecret = jwtSecret;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http
    ) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(exchange -> exchange

                        .pathMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        )
                        .permitAll()

                        .pathMatchers(
                                HttpMethod.POST,
                                "/auth/register",
                                "/auth/login",
                                "/auth/verify-otp",
                                "/auth/forgot-password",
                                "/auth/reset-password"
                        )
                        .permitAll()

                        .pathMatchers(
                                HttpMethod.GET,
                                "/actuator/health",
                                "/actuator/health/**",
                                "/actuator/info"
                        )
                        .permitAll()

                        .pathMatchers(
                                HttpMethod.POST,
                                "/auth/payers",
                                "/auth/providers"
                        )
                        .hasRole("ADMIN")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/payers/me"
                        )
                        .hasRole("PAYER")

                        .pathMatchers(
                                HttpMethod.PUT,
                                "/api/payers/me"
                        )
                        .hasRole("PAYER")

                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/payers"
                        )
                        .hasRole("ADMIN")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/payers"
                        )
                        .hasRole("ADMIN")

                        .pathMatchers(
                                HttpMethod.PATCH,
                                "/api/payers/*/status"
                        )
                        .hasRole("ADMIN")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/payers/*"
                        )
                        .hasRole("ADMIN")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/plans/available",
                                "/api/plans/available/*"
                        )
                        .hasRole("MEMBER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/plans/admin",
                                "/api/plans/admin/*"
                        )
                        .hasRole("ADMIN")

                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/plans"
                        )
                        .hasRole("PAYER")

                        .pathMatchers(
                                HttpMethod.PATCH,
                                "/api/plans/me/*/status"
                        )
                        .hasRole("PAYER")

                        .pathMatchers(
                                HttpMethod.PUT,
                                "/api/plans/me/*"
                        )
                        .hasRole("PAYER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/plans/me",
                                "/api/plans/me/*"
                        )
                        .hasRole("PAYER")


                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/members/me"
                        )
                        .hasRole("MEMBER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/members/me"
                        )
                        .hasRole("MEMBER")

                        .pathMatchers(
                                HttpMethod.PUT,
                                "/api/members/me"
                        )
                        .hasRole("MEMBER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/members"
                        )
                        .hasRole("ADMIN")

                        .pathMatchers(
                                HttpMethod.PATCH,
                                "/api/members/*/status"
                        )
                        .hasRole("ADMIN")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/members/*"
                        )
                        .hasRole("ADMIN")


                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/enrollments"
                        )
                        .hasRole("MEMBER")

                        .pathMatchers(
                                HttpMethod.PATCH,
                                "/api/enrollments/me/*/cancel"
                        )
                        .hasRole("MEMBER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/enrollments/me",
                                "/api/enrollments/me/*"
                        )
                        .hasRole("MEMBER")

                        .pathMatchers(
                                HttpMethod.PATCH,
                                "/api/enrollments/payer/*/approve",
                                "/api/enrollments/payer/*/reject"
                        )
                        .hasRole("PAYER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/enrollments/payer",
                                "/api/enrollments/payer/*"
                        )
                        .hasRole("PAYER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/policies/me",
                                "/api/policies/me/*"
                        )
                        .hasRole("MEMBER")

                        .pathMatchers(
                                HttpMethod.PATCH,
                                "/api/policies/payer/*/status"
                        )
                        .hasRole("PAYER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/policies/payer",
                                "/api/policies/payer/*"
                        )
                        .hasRole("PAYER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/providers/me"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.PUT,
                                "/api/providers/me"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/providers/available",
                                "/api/providers/available/*"
                        )
                        .hasRole("MEMBER")

                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/providers"
                        )
                        .hasRole("ADMIN")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/providers"
                        )
                        .hasRole("ADMIN")

                        .pathMatchers(
                                HttpMethod.PATCH,
                                "/api/providers/*/status"
                        )
                        .hasRole("ADMIN")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/providers/*"
                        )
                        .hasRole("ADMIN")


                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/departments"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.PATCH,
                                "/api/departments/provider/*/status"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.PUT,
                                "/api/departments/provider/*"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/departments/provider",
                                "/api/departments/provider/*"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/departments/available/provider/*",
                                "/api/departments/available/*"
                        )
                        .hasRole("MEMBER")


                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/doctors"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.PATCH,
                                "/api/doctors/provider/*/status"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.PUT,
                                "/api/doctors/provider/*"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/doctors/provider",
                                "/api/doctors/provider/*"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/doctors/available",
                                "/api/doctors/available/*",
                                "/api/doctors/available/provider/*",
                                "/api/doctors/available/department/*"
                        )
                        .hasRole("MEMBER")
                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/availability"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.PATCH,
                                "/api/availability/provider/*/status"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.PUT,
                                "/api/availability/provider/*"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.DELETE,
                                "/api/availability/provider/*"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/availability/provider",
                                "/api/availability/provider/*",
                                "/api/availability/provider/doctor/*"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/availability/available/doctor/*"
                        )
                        .hasRole("MEMBER")
                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/appointments"
                        )
                        .hasRole("MEMBER")

                        .pathMatchers(
                                HttpMethod.PATCH,
                                "/api/appointments/me/*/cancel"
                        )
                        .hasRole("MEMBER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/appointments/me",
                                "/api/appointments/me/*"
                        )
                        .hasRole("MEMBER")

                        .pathMatchers(
                                HttpMethod.PATCH,
                                "/api/appointments/provider/*/confirm",
                                "/api/appointments/provider/*/cancel",
                                "/api/appointments/provider/*/complete",
                                "/api/appointments/provider/*/no-show"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/appointments/provider",
                                "/api/appointments/provider/*"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/appointments/slots/doctor/*"
                        )
                        .hasRole("MEMBER")


                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/medical-records"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.PUT,
                                "/api/medical-records/provider/*"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/medical-records/provider",
                                "/api/medical-records/provider/*",
                                "/api/medical-records/provider/doctor/*"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/medical-records/me",
                                "/api/medical-records/me/*"
                        )
                        .hasRole("MEMBER")


                        .pathMatchers(
                                HttpMethod.POST,
                                "/api/claims"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.PATCH,
                                "/api/claims/provider/*/cancel"
                        )
                        .hasRole("PROVIDER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/claims/provider",
                                "/api/claims/provider/*"
                        )
                        .hasRole("PROVIDER")


                        .pathMatchers(
                                HttpMethod.PATCH,
                                "/api/claims/payer/*/review",
                                "/api/claims/payer/*/approve",
                                "/api/claims/payer/*/reject"
                        )
                        .hasRole("PAYER")


                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/claims/payer",
                                "/api/claims/payer/*"
                        )
                        .hasRole("PAYER")


                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/claims/me",
                                "/api/claims/me/*"
                        )
                        .hasRole("MEMBER")

                        .pathMatchers(
                                HttpMethod.GET,
                                "/api/notifications/me",
                                "/api/notifications/me/unread",
                                "/api/notifications/me/unread-count"
                        )
                        .hasAnyRole(
                                "MEMBER",
                                "PROVIDER",
                                "PAYER",
                                "ADMIN"
                        )

                        .pathMatchers(
                                HttpMethod.PATCH,
                                "/api/notifications/me/read-all",
                                "/api/notifications/me/*/read"
                        )
                        .hasAnyRole(
                                "MEMBER",
                                "PROVIDER",
                                "PAYER",
                                "ADMIN"
                        )
                                .anyExchange()
                                .authenticated()
                )

                .oauth2ResourceServer(resourceServer ->
                        resourceServer.jwt(jwt ->
                                jwt
                                        .jwtDecoder(
                                                reactiveJwtDecoder()
                                        )
                                        .jwtAuthenticationConverter(
                                                jwtAuthenticationConverter()
                                        )
                        )
                )

                .build();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {

        byte[] secretBytes =
                jwtSecret.getBytes(StandardCharsets.UTF_8);

        SecretKey secretKey =
                new SecretKeySpec(
                        secretBytes,
                        "HmacSHA384"
                );

        return NimbusReactiveJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS384)
                .build();
    }

    @Bean
    public Converter<
            Jwt,
            Mono<AbstractAuthenticationToken>
            > jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            String role =
                    jwt.getClaimAsString("role");

            if (role == null || role.isBlank()) {
                return List.of();
            }

            return List.of(
                    new SimpleGrantedAuthority(role)
            );
        });

        return new ReactiveJwtAuthenticationConverterAdapter(
                converter
        );
    }
}