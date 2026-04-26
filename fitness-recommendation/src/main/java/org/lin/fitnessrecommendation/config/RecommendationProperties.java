package org.lin.fitnessrecommendation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "recommendation")
public class RecommendationProperties {

    /** 规则引擎权重 */
    private double ruleWeight = 0.4;

    /** 协同过滤权重 */
    private double cfWeight = 0.6;

    /** 协同过滤最近邻数量 */
    private int cfTopK = 10;

    /** 最大推荐数量 */
    private int maxRecommendations = 10;
}
