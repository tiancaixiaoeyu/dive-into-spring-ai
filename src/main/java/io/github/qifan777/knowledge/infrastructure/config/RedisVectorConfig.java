package io.github.qifan777.knowledge.infrastructure.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.autoconfigure.vectorstore.redis.RedisVectorStoreAutoConfiguration;
import org.springframework.ai.autoconfigure.vectorstore.redis.RedisVectorStoreProperties;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.qifan777.knowledge.infrastructure.vector.NoOpVectorStore;
import redis.clients.jedis.JedisPooled;

@Slf4j

@Configuration
@EnableAutoConfiguration(exclude = {RedisVectorStoreAutoConfiguration.class})
@EnableConfigurationProperties({RedisVectorStoreProperties.class})
@AllArgsConstructor
public class RedisVectorConfig {


    /**
     * 创建RedisStack向量数据库
     *
     * @param embeddingModel 嵌入模型
     * @param properties     redis-stack的配置信息
     * @return vectorStore 向量数据库
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel,
                                   RedisVectorStoreProperties properties,
                                   RedisConnectionDetails redisConnectionDetails) {
        RedisVectorStore.RedisVectorStoreConfig config = RedisVectorStore.RedisVectorStoreConfig.builder()
                .withIndexName(properties.getIndex())
                .withPrefix(properties.getPrefix())
                .build();
        try {
            JedisPooled jedis = new JedisPooled(
                    redisConnectionDetails.getStandalone().getHost(),
                    redisConnectionDetails.getStandalone().getPort(),
                    redisConnectionDetails.getUsername(),
                    redisConnectionDetails.getPassword()
            );
            jedis.ping();
            RedisVectorStore redisVectorStore = new RedisVectorStore(config, embeddingModel, jedis, properties.isInitializeSchema());
            redisVectorStore.afterPropertiesSet();
            return redisVectorStore;
        } catch (Exception e) {
            log.warn("Redis 未就绪或未安装 RedisStack 搜索模块，使用 NoOpVectorStore 以避免启动失败", e);
            return new NoOpVectorStore();
        }
    }
}

