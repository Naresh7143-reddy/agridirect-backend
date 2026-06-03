package com.agridirect;

import com.cloudinary.Cloudinary;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import static org.mockito.Mockito.mock;

/**
 * Replaces Cloudinary and Redis beans with mocks for integration tests.
 * No real network connections are made.
 */
@TestConfiguration
public class TestConfig {

    /** Mock Cloudinary so upload endpoints compile but don't hit the network. */
    @Bean
    @Primary
    public Cloudinary cloudinary() {
        return mock(Cloudinary.class);
    }

    /** Mock Redis connection so tests run without a Redis server. */
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return mock(RedisConnectionFactory.class);
    }

    @Bean
    @Primary
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        return mock(RedisTemplate.class);
    }
}
