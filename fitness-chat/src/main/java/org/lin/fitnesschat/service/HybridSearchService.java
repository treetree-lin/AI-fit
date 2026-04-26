package org.lin.fitnesschat.service;

/**
 * @author lin
 * @date 2026-04-17
 */

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import org.lin.fitnesschat.client.EmbeddingClient;
import org.lin.fitnesschat.entity.EsDocument;
import org.lin.fitnesschat.entity.FileUpload;
import org.lin.fitnesschat.entity.SearchResult;
import org.lin.fitnesschat.repository.FileUploadRepository;
import org.lin.fitnesscommon.exception.CustomException;
import org.lin.fitnesscommon.entity.User;
import org.lin.fitnessuser.repository.UserRepository;
import org.lin.fitnessuser.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 混合搜索服务，结合文本匹配和向量相似度搜索
 * 支持权限过滤，确保用户只能搜索其有权限访问的文档
 */
@Service
public class HybridSearchService {

    private static final Logger logger = LoggerFactory.getLogger(HybridSearchService.class);

    @Autowired
    private ElasticsearchClient esClient;

    @Autowired
    private EmbeddingClient embeddingClient;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileUploadRepository fileUploadRepository;

    @Value("${elasticsearch.host:localhost}")
    private String elasticsearchHost;

    @Value("${elasticsearch.port:9200}")
    private int elasticsearchPort;

    /**
     * 使用文本匹配和向量相似度进行混合搜索，支持权限过滤
     * 该方法确保用户只能搜索其有权限访问的文档（自己的文档、公开文档、所属组织的文档）
     *
     * @param query  查询字符串
     * @param userId 用户ID
     * @param topK   返回结果数量
     * @return 搜索结果列表
     */
    public List<SearchResult> searchWithPermission(String query, String userId, int topK) {
        logger.debug("开始带权限搜索，查询: {}, 用户ID: {}", query, userId);

        try {

            // 获取用户的数据库ID用于权限过滤
            String userDbId = getUserDbId(userId);
            logger.debug("用户 {} 的数据库ID: {}", userId, userDbId);

            // 生成查询向量
            final List<Float> queryVector = embedToVectorList(query);

            // 如果向量生成失败，仅使用文本匹配
            if (queryVector == null) {
                logger.warn("向量生成失败，仅使用文本匹配进行搜索");
                return textOnlySearchWithPermission(query, userDbId, topK);
            }

            logger.debug("向量生成成功，开始执行混合搜索 KNN");

            SearchResponse<EsDocument> response = esClient.search(s -> {
                s.index("knowledge_base");
                // KNN 召回
                int recallK = topK * 30; // KNN 召回窗口
                s.knn(kn -> kn
                        .field("vector")
                        .queryVector(queryVector)
                        .k(recallK)
                        .numCandidates(recallK)
                );
                // 必须命中关键词 + 权限过滤
                s.query(q -> q.bool(b -> b
                        .must(mst -> mst.match(m -> m.field("textContent").query(query)))
                        .filter(f -> f.bool(bf -> bf
                                // 条件1: 用户可访问自己的文档
                                .should(s1 -> s1.term(t -> t.field("userId").value(userDbId)))
                                // 条件2: 公开文档
                                .should(s2 -> s2.term(t -> t.field("public").value(true)))

                        ))
                ));

                // 第二阶段 BM25 rescore
                s.rescore(r -> r
                        .windowSize(recallK)
                        .query(rq -> rq
                                .queryWeight(0.2d)               // 保留部分 KNN 分
                                .rescoreQueryWeight(1.0d)        // BM25 主导
                                .query(rqq -> rqq.match(m -> m
                                        .field("textContent")
                                        .query(query)
                                        .operator(Operator.And)
                                ))
                        )
                );
                s.size(topK);
                return s;
            }, EsDocument.class);

            logger.debug("Elasticsearch查询执行完成，命中数量: {}, 最大分数: {}",
                    response.hits().total().value(), response.hits().maxScore());

            List<SearchResult> results = response.hits().hits().stream()
                    .map(hit -> {
                        assert hit.source() != null;
                        logger.debug("搜索结果 - 文件: {}, 块: {}, 分数: {}, 内容: {}",
                                hit.source().getFileMd5(), hit.source().getChunkId(), hit.score(),
                                hit.source().getTextContent().substring(0, Math.min(50, hit.source().getTextContent().length())));
                        return new SearchResult(
                                hit.source().getFileMd5(),
                                hit.source().getChunkId(),
                                hit.source().getTextContent(),
                                hit.score(),
                                hit.source().getUserId(),
                                hit.source().isPublic()
                        );
                    })
                    .toList();

            logger.debug("返回搜索结果数量: {}", results.size());
            attachFileNames(results);
            return results;
        } catch (Exception e) {
            logger.error("带权限的搜索失败: {}", e.getMessage());
            // 检查是否是连接问题
            if (e.getMessage() != null && e.getMessage().contains("Connection is closed")) {
                logger.warn("Elasticsearch 连接失败，请检查 ES 服务是否启动 (地址: {}:{})",
                        elasticsearchHost, elasticsearchPort);
            }
            // 发生异常时尝试使用纯文本搜索作为后备方案
            try {
                logger.info("尝试使用纯文本搜索作为后备方案");
                return textOnlySearchWithPermission(query, getUserDbId(userId), topK);
            } catch (Exception fallbackError) {
                logger.error("后备搜索也失败: {}", fallbackError.getMessage());
                // 如果 ES 完全不可用，返回空列表而不是抛出异常
                logger.warn("Elasticsearch 服务不可用，返回空搜索结果。请确保 ES 服务已启动并可以访问。");
                return Collections.emptyList();
            }
        }
    }

    /**
     * 仅使用文本匹配的带权限搜索方法
     */
     private List<SearchResult> textOnlySearchWithPermission(String query, String userDbId,  int topK) {
        try {
            logger.debug("开始执行纯文本搜索，用户数据库ID: {}, 标签: {}", userDbId);

            SearchResponse<EsDocument> response = esClient.search(s -> s
                            .index("knowledge_base")
                            .query(q -> q
                                    .bool(b -> b
                                            // 匹配内容相关性
                                            .must(m -> m
                                                    .match(ma -> ma
                                                            .field("textContent")
                                                            .query(query)
                                                    )
                                            )
                                            // 权限过滤
                                            .filter(f -> f
                                                    .bool(bf -> bf
                                                            // 条件1: 用户可以访问自己的文档
                                                            .should(s1 -> s1
                                                                    .term(t -> t
                                                                            .field("userId")
                                                                            .value(userDbId)
                                                                    )
                                                            )
                                                            // 条件2: 用户可以访问公开的文档
                                                            .should(s2 -> s2
                                                                    .term(t -> t
                                                                            .field("public")
                                                                            .value(true)
                                                                    )
                                                            )
                                                            .minimumShouldMatch("1")
                                                    )
                                            )
                                    )
                            )
                            .minScore(0.3d)
                            .size(topK),
                    EsDocument.class
            );

            logger.debug("纯文本查询执行完成，命中数量: {}, 最大分数: {}",
                    response.hits().total().value(), response.hits().maxScore());

            List<SearchResult> results = response.hits().hits().stream()
                    .map(hit -> {
                        assert hit.source() != null;
                        logger.debug("纯文本搜索结果 - 文件: {}, 块: {}, 分数: {}, 内容: {}",
                                hit.source().getFileMd5(), hit.source().getChunkId(), hit.score(),
                                hit.source().getTextContent().substring(0, Math.min(50, hit.source().getTextContent().length())));
                        return new SearchResult(
                                hit.source().getFileMd5(),
                                hit.source().getChunkId(),
                                hit.source().getTextContent(),
                                hit.score(),
                                hit.source().getUserId(),
                                hit.source().isPublic()
                        );
                    })
                    .toList();

            logger.debug("返回纯文本搜索结果数量: {}", results.size());
            attachFileNames(results);
            return results;
        } catch (Exception e) {
            logger.error("纯文本搜索失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 原始搜索方法，不包含权限过滤，保留向后兼容性
     */
    public List<SearchResult> search(String query, int topK) {
        try {
            logger.debug("开始混合检索，查询: {}, topK: {}", query, topK);
            logger.warn("使用了没有权限过滤的搜索方法，建议使用 searchWithPermission 方法");

            // 生成查询向量
            final List<Float> queryVector = embedToVectorList(query);

            // 如果向量生成失败，仅使用文本匹配
            if (queryVector == null) {
                logger.warn("向量生成失败，仅使用文本匹配进行搜索");
                return textOnlySearch(query, topK);
            }

            SearchResponse<EsDocument> response = esClient.search(s -> {
                s.index("knowledge_base");
                int recallK = topK * 30;
                s.knn(kn -> kn
                        .field("vector")
                        .queryVector(queryVector)
                        .k(recallK)
                        .numCandidates(recallK)
                );

                // 过滤仅保留包含关键词的文本
                s.query(q -> q.match(m -> m.field("textContent").query(query)));

                // rescore BM25
                s.rescore(r -> r
                        .windowSize(recallK)
                        .query(rq -> rq
                                .queryWeight(0.2d)
                                .rescoreQueryWeight(1.0d)
                                .query(rqq -> rqq.match(m -> m
                                        .field("textContent")
                                        .query(query)
                                        .operator(Operator.And)
                                ))
                        )
                );
                s.size(topK);
                return s;
            }, EsDocument.class);

            return response.hits().hits().stream()
                    .map(hit -> {
                        assert hit.source() != null;
                        return new SearchResult(
                                hit.source().getFileMd5(),
                                hit.source().getChunkId(),
                                hit.source().getTextContent(),
                                hit.score()
                        );
                    })
                    .toList();
        } catch (Exception e) {
            logger.error("搜索失败", e);
            // 发生异常时尝试使用纯文本搜索作为后备方案
            try {
                logger.info("尝试使用纯文本搜索作为后备方案");
                return textOnlySearch(query, topK);
            } catch (Exception fallbackError) {
                logger.error("后备搜索也失败", fallbackError);
                throw new RuntimeException("搜索完全失败", fallbackError);
            }
        }
    }

    /**
     * 仅使用文本匹配的搜索方法
     */
    private List<SearchResult> textOnlySearch(String query, int topK) throws Exception {
        SearchResponse<EsDocument> response = esClient.search(s -> s
                        .index("knowledge_base")
                        .query(q -> q
                                .match(m -> m
                                        .field("textContent")
                                        .query(query)
                                )
                        )
                        .size(topK),
                EsDocument.class
        );

        return response.hits().hits().stream()
                .map(hit -> {
                    assert hit.source() != null;
                    return new SearchResult(
                            hit.source().getFileMd5(),
                            hit.source().getChunkId(),
                            hit.source().getTextContent(),
                            hit.score()
                    );
                })
                .toList();
    }

    /**
     * 生成查询向量，返回 List<Float>，失败时返回 null
     */
    private List<Float> embedToVectorList(String text) {
        try {
            logger.debug("开始生成查询向量");
            List<float[]> vecs = embeddingClient.embed(List.of(text));
            if (vecs == null || vecs.isEmpty()) {
                logger.warn("生成的向量为空，将使用纯文本搜索");
                return null;
            }
            float[] raw = vecs.get(0);
            List<Float> list = new ArrayList<>(raw.length);
            for (float v : raw) {
                list.add(v);
            }
            logger.debug("向量生成成功，维度: {}", list.size());
            return list;
        } catch (Exception e) {
            logger.error("生成向量失败: {}", e.getMessage(), e);
            return null;
        }
    }


    /**
     * 获取用户的数据库ID用于权限过滤
     */
    private String getUserDbId(String userId) {
        logger.debug("获取用户数据库ID，用户ID: {}", userId);
        try {
            // 获取用户名
            User user;
            try {
                Long userIdLong = Long.parseLong(userId);
                logger.debug("解析用户ID为Long: {}", userIdLong);
                user = userRepository.findById(userIdLong)
                        .orElseThrow(() -> new CustomException("User not found with ID: " + userId, HttpStatus.NOT_FOUND));
                logger.debug("通过ID找到用户: {}", user.getUsername());
                return userIdLong.toString(); // 如果输入已经是数字ID，直接返回
            } catch (NumberFormatException e) {
                // 如果userId不是数字格式，则假设它就是username
                logger.debug("用户ID不是数字格式，作为用户名查找: {}", userId);
                user = userRepository.findByUsername(userId)
                        .orElseThrow(() -> new CustomException("User not found: " + userId, HttpStatus.NOT_FOUND));
                logger.debug("通过用户名找到用户: {}, ID: {}", user.getUsername(), user.getId());
                return user.getId().toString(); // 返回用户的数据库ID
            }
        } catch (Exception e) {
            logger.error("获取用户数据库ID失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取用户数据库ID失败", e);
        }
    }

    private void attachFileNames(List<SearchResult> results) {
        if (results == null || results.isEmpty()) {
            return;
        }
        try {
            // 收集所有唯一的 fileMd5
            Set<String> md5Set = results.stream()
                    .map(SearchResult::getFileMd5)
                    .collect(Collectors.toSet());
            List<FileUpload> uploads = fileUploadRepository.findByFileMd5In(new java.util.ArrayList<>(md5Set));
            Map<String, String> md5ToName = uploads.stream()
                    .collect(Collectors.toMap(FileUpload::getFileMd5, FileUpload::getFileName));
            // 填充文件名
            results.forEach(r -> r.setFileName(md5ToName.get(r.getFileMd5())));
        } catch (Exception e) {
            logger.error("补充文件名失败", e);
        }
    }
}
