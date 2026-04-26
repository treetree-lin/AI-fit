package org.lin.fitnesschat.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;


// Elasticsearch客户端配置类
@Configuration
public class EsConfig {

    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private int port;

    @Value("${elasticsearch.scheme:https}")
    private String scheme;

    @Value("${elasticsearch.username:elastic}")
    private String username;

    @Value("${elasticsearch.password:changeme}")
    private String password;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // 创建低级客户端
        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, scheme));

        // 设置基本认证 + 连接保活/重试/超时
        if (username != null && !username.isEmpty()) {
            BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                // 忽略 TLS 证书（仅限开发环境）
                try {
                    SSLContext sslContext = SSLContexts.custom()
                            .loadTrustMaterial(null, (X509Certificate[] chain, String authType) -> true)
                            .build();
                    httpClientBuilder.setSSLContext(sslContext);
                    httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                } catch (Exception e) {
                    // ignore
                }
                // 连接保活与 I/O 超时配置，防止长连接被防火墙/NAT 断开
                httpClientBuilder.setDefaultIOReactorConfig(
                        IOReactorConfig.custom()
                                .setSoKeepAlive(true)
                                .setConnectTimeout(5000)
                                .setSoTimeout(30000)
                                .build()
                );
                // 连接池存活检测：空闲 5 秒即重新校验，避免复用已关闭连接
                httpClientBuilder.setConnectionTimeToLive(5, TimeUnit.SECONDS);
                return httpClientBuilder.setDefaultCredentialsProvider(credsProvider);
            });
            // 请求级别重试：节点失败时自动重试其他节点
            builder.setFailureListener(new RestClient.FailureListener() {
                @Override
                public void onFailure(org.elasticsearch.client.Node node) {
                    // 可在此处加日志，RestClient 内部已具备重试逻辑
                }
            });
        }

        RestClient restClient = builder.build();

        // 创建传输层
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper()
        );

        // 返回高级客户端
        return new ElasticsearchClient(transport);
    }
}
