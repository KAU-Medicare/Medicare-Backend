package com.example.kaumedicare.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class ApiConfig {

    private static final Logger logger = LoggerFactory.getLogger(ApiConfig.class);

    @Bean
    public RestTemplate restTemplate() {
        logger.info("Configuring RestTemplate bean with UTF-8 message converter");
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                logger.error("Response error: {} - {}", response.getStatusCode(), response.getStatusText());
                super.handleError(response);
            }
        });
        return restTemplate;
    }

    /*
    @Bean
    public ObjectMapper objectMapper() { return new ObjectMapper(); }
     */
}