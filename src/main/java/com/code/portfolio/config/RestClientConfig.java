package com.code.portfolio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** Configura o {@link RestClient} usado para consumir a API externa de membros. */
@Configuration
public class RestClientConfig {

    @Bean
    RestClient membersRestClient(@Value("${members.api.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
