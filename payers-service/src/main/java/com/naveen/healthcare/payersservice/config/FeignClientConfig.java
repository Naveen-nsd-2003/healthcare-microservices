package com.naveen.healthcare.payersservice.config;

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

            ServletRequestAttributes requestAttributes =
                    (ServletRequestAttributes)
                            RequestContextHolder.getRequestAttributes();

            if (requestAttributes == null) {
                return;
            }

            HttpServletRequest currentRequest =
                    requestAttributes.getRequest();

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