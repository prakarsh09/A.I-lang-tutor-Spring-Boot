package com.example.tutor.tutor.config;

import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenAiConfig {
     private static final String OPENAI_BASE_URL = "https://api.openai.com"; // Example base URL

     @Value("${openai.key}")
private String openAikey;

    @Bean
    public OpenAiAudioApi openAiAudioApi(RestClient.Builder restClientBuilder, ResponseErrorHandler responseErrorHandler) {
        String openAiToken = openAikey; 

        return new OpenAiAudioApi(OPENAI_BASE_URL, openAiToken, restClientBuilder, responseErrorHandler);
    }
}
