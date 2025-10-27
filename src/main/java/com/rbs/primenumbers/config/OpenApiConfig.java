package com.rbs.primenumbers.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI primesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Prime Numbers API")
                        .version("v1")
                        .description("REST API that computes all primes ≤ max"));
    }
}
