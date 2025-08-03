package com.readingclub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient aladinWebClient() {
        return WebClient.builder()
                .baseUrl("http://www.aladin.co.kr/ttb/api")
                .defaultHeader("Accept", "application/json, text/javascript, */*; q=0.01")
                .defaultHeader("Accept-Charset", "UTF-8")
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/115.0 Safari/537.36")
                .build();
    }
}
