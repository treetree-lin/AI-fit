package org.lin.fitnesschat.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${embedding.api.url}")
    private String apiUrl;

    @Value("${embedding.api.key}")
    private String apiKey;

    /**
     * 带超时配置的 Reactor HTTP 连接器
     * 供 LlmClient 等需要使用自定义 HTTP 连接器的组件注入
     */
    @Bean
    public ReactorClientHttpConnector reactorClientHttpConnector() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000) // 连接超时 30s
                .responseTimeout(Duration.ofSeconds(120));           // 响应超时 120s
        return new ReactorClientHttpConnector(httpClient);
    }

    @Bean
    public WebClient embeddingWebClient(ReactorClientHttpConnector httpConnector) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(16 * 1024 * 1024)) // 16MB
            .build();

        return WebClient.builder()
            .baseUrl(apiUrl)
            .clientConnector(httpConnector) // 使用带超时配置的 HTTP 连接器
            .exchangeStrategies(strategies)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
} 