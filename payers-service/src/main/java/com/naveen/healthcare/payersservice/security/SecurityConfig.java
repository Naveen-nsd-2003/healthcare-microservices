package com.naveen.healthcare.payersservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {


    private final String jwtSecret;


    public SecurityConfig(

            @Value("${jwt.secret}")

            String jwtSecret

    ) {

        this.jwtSecret = jwtSecret;
    }


    // =====================================================
    // SECURITY FILTER CHAIN
    // =====================================================

    @Bean
    public SecurityFilterChain securityFilterChain(

            HttpSecurity http

    ) throws Exception {


        http

                .csrf(csrf -> csrf.disable())


                .authorizeHttpRequests(auth -> auth


                        // -----------------------------------------
                        // PUBLIC ACTUATOR ENDPOINTS
                        // -----------------------------------------

                        .requestMatchers(

                                "/actuator/health",

                                "/actuator/info"

                        )
                        .permitAll()


                        // -----------------------------------------
                        // ALL BUSINESS ENDPOINTS
                        // -----------------------------------------

                        .anyRequest()
                        .authenticated()

                )


                // -----------------------------------------
                // JWT VALIDATION
                // -----------------------------------------

                .oauth2ResourceServer(resourceServer ->

                        resourceServer.jwt(jwt -> jwt

                                .decoder(
                                        jwtDecoder()
                                )

                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter()
                                )

                        )

                );


        return http.build();
    }


    // =====================================================
    // JWT DECODER
    // =====================================================

    @Bean
    public JwtDecoder jwtDecoder() {


        byte[] secretBytes = jwtSecret.getBytes(

                StandardCharsets.UTF_8

        );


        SecretKey secretKey = new SecretKeySpec(

                secretBytes,

                "HmacSHA384"

        );


        return NimbusJwtDecoder

                .withSecretKey(secretKey)

                .macAlgorithm(
                        MacAlgorithm.HS384
                )

                .build();
    }


    // =====================================================
    // ROLE CONVERTER
    // =====================================================

    @Bean
    public Converter<
            Jwt,
            ? extends AbstractAuthenticationToken
            > jwtAuthenticationConverter() {


        JwtAuthenticationConverter converter =

                new JwtAuthenticationConverter();


        converter.setJwtGrantedAuthoritiesConverter(jwt -> {


            String role = jwt.getClaimAsString(

                    "role"

            );


            if (role == null || role.isBlank()) {

                return List.of();
            }


            return List.of(

                    new SimpleGrantedAuthority(
                            role
                    )

            );

        });


        return converter;
    }
}