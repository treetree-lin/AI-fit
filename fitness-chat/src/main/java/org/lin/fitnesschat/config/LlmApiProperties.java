package org.lin.fitnesschat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 大语言模型 API 配置类
 * 默认对接 Kimi API (Moonshot)，兼容 OpenAI 格式
 * 也可配置为其他兼容 OpenAI API 格式的服务（如 DeepSeek、OpenAI、Ollama 等）
 */
@Data
@Component
@ConfigurationProperties(prefix = "llm.api")
public class LlmApiProperties {
    /** API 基础地址，Kimi 为 https://api.moonshot.cn/v1 */
    private String url;
    /** 模型名称，Kimi 可选 moonshot-v1-8k / moonshot-v1-32k / moonshot-v1-128k */
    private String model;
    /** API Key，从 Kimi 开放平台获取 */
    private String key = "sk-kimi-89LPecs5ec3udYSeFqVssKUkqMKIqTV6EQpLXoxiaIIeUweYHgiSRqAy6IFIxr2b";
}
