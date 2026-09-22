package com.naveen.healthcare.claimservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration(proxyBeanMethods = false)
public class FeignClientConfig {

    @Bean
    public RequestInterceptor authorizationHeaderInterceptor() {

        return requestTemplate -> {

            if (!(RequestContextHolder.getRequestAttributes()
                    instanceof ServletRequestAttributes attributes)) {
                return;
            }

            HttpServletRequest currentRequest =
                    attributes.getRequest();

            String authorizationHeader =
                    currentRequest.getHeader(
                            HttpHeaders.AUTHORIZATION
                    );

            if (authorizationHeader == null
                    || authorizationHeader.isBlank()) {
                return;
            }

            if (!authorizationHeader.startsWith("Bearer ")) {
                return;
            }

            requestTemplate.header(
                    HttpHeaders.AUTHORIZATION,
                    authorizationHeader
            );
        };
    }
}
