package com.bing.elk.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import redis.clients.jedis.JedisPooled;

/**
 * redis 配置
 * @author bing
 * @desc jedis
 */
@Configuration
//@ConfigurationProperties(prefix = "redis")
public class RedisConfiguration {
    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private int port;

    @Bean(destroyMethod = "close",name = "pool")
    public JedisPooled initConnection(){
        JedisPooled pool = new JedisPooled(host, port);
        return pool;
    }
}
