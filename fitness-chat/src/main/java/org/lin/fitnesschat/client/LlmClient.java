package org.lin.fitnesschat.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lin.fitnesschat.config.AiProperties;
import org.lin.fitnesschat.config.LlmApiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 大语言模型客户端
 * 兼容 OpenAI API 格式，默认对接 Kimi (Moonshot) API
 * 支持流式响应 (SSE) 处理
 */
@Service
public class LlmClient {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    private final AiProperties aiProperties;
    private static final Logger logger = LoggerFactory.getLogger(LlmClient.class);

    public LlmClient(LlmApiProperties llmProps,
                     AiProperties aiProperties,
                     ReactorClientHttpConnector httpConnector) {
        String apiUrl = llmProps.getUrl();
        this.apiKey = llmProps.getKey();
        this.model = llmProps.getModel();
        this.aiProperties = aiProperties;

        if (apiUrl == null || apiUrl.isEmpty()) {
            throw new IllegalArgumentException("llm.api.url 未配置");
        }

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(apiUrl)
                .clientConnector(httpConnector)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)); // 16MB

        if (apiKey != null && !apiKey.trim().isEmpty()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
        }

        this.webClient = builder.build();
        logger.info("LlmClient 初始化完成，URL: {}, 模型: {}, 读取超时: 120s", apiUrl, this.model);
    }

    public void streamResponse(String userMessage,
                               String context,
                               List<Map<String, String>> history,
                               Consumer<String> onChunk,
                               Consumer<Throwable> onError,
                               Runnable onComplete) {

        Map<String, Object> request = buildRequest(userMessage, context, history);

        webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(
                        status -> status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException(
                                        String.format("LLM 服务器错误 [%d]: %s\n请求模型: %s\n请检查 Ollama 服务是否正常运行",
                                                response.statusCode().value(),
                                                body,
                                                model
                                        )
                                ))
                )
                .onStatus(
                        status -> status.is4xxClientError(),
                        response -> response.bodyToMono(String.class)
                                .map(body -> new RuntimeException(
                                        String.format("LLM 客户端错误 [%d]: %s\n请检查 API Key 和模型配置",
                                                response.statusCode().value(),
                                                body
                                        )
                                ))
                )
                .bodyToFlux(String.class)
                .timeout(Duration.ofSeconds(120))
                .subscribe(
                        chunk -> processChunk(chunk, onChunk),
                        error -> {
                            logger.error("流式响应错误: {}", error.getMessage(), error);
                            onError.accept(error);
                        },
                        onComplete
                );
    }

    private Map<String, Object> buildRequest(String userMessage,
                                             String context,
                                             List<Map<String, String>> history) {
        logger.info("构建LLM请求，用户消息：{}，上下文长度：{}，历史消息数：{}",
                userMessage,
                context != null ? context.length() : 0,
                history != null ? history.size() : 0);

        Map<String, Object> request = new java.util.HashMap<>();
        request.put("model", model);
        request.put("messages", buildMessages(userMessage, context, history));
        request.put("stream", true);

        AiProperties.Generation gen = aiProperties.getGeneration();
        if (gen.getTemperature() != null) {
            request.put("temperature", gen.getTemperature());
        }
        if (gen.getTopP() != null) {
            request.put("top_p", gen.getTopP());
        }
        if (gen.getMaxTokens() != null) {
            request.put("max_tokens", gen.getMaxTokens());
        }
        return request;
    }

    private List<Map<String, String>> buildMessages(String userMessage,
                                                    String context,
                                                    List<Map<String, String>> history) {
        List<Map<String, String>> messages = new ArrayList<>();

        AiProperties.Prompt promptCfg = aiProperties.getPrompt();

        StringBuilder sysBuilder = new StringBuilder();
        String rules = promptCfg.getRules();
        if (rules != null) {
            sysBuilder.append(rules).append("\n\n");
        }

        String refStart = promptCfg.getRefStart() != null ? promptCfg.getRefStart() : "<<REF>>";
        String refEnd = promptCfg.getRefEnd() != null ? promptCfg.getRefEnd() : "<<END>>";
        sysBuilder.append(refStart).append("\n");

        if (context != null && !context.isEmpty()) {
            sysBuilder.append(context);
        } else {
            String noResult = promptCfg.getNoResultText() != null ? promptCfg.getNoResultText() : "（本轮无检索结果）";
            sysBuilder.append(noResult).append("\n");
        }

        sysBuilder.append(refEnd);

        messages.add(Map.of("role", "system", "content", sysBuilder.toString()));

        if (history != null && !history.isEmpty()) {
            messages.addAll(history);
        }

        messages.add(Map.of("role", "user", "content", userMessage));

        return messages;
    }

    private void processChunk(String chunk, Consumer<String> onChunk) {
        try {
            if (chunk == null || chunk.isEmpty()) {
                return;
            }

            // 处理 SSE 格式：Kimi / OpenAI 兼容 API 返回的流式数据每行以 "data: " 开头
            String data = chunk;
            if (data.startsWith("data: ")) {
                data = data.substring(6).trim();
            }

            if ("[DONE]".equals(data)) {
                logger.debug("流式响应结束");
                return;
            }

            if (data.isEmpty()) {
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(data);
            String content = node.path("choices").path(0).path("delta").path("content").asText("");

            if (!content.isEmpty()) {
                onChunk.accept(content);
            }
        } catch (Exception e) {
            // 忽略无法解析的非 JSON 行（如 SSE 空行或心跳）
            logger.debug("处理数据块时跳过非 JSON 内容: {}", chunk);
        }
    }
}
