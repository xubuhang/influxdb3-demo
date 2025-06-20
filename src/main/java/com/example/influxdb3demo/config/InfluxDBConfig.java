package com.example.influxdb3demo.config;

import com.influxdb.v3.client.InfluxDBClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "influxdb")
@Data
public class InfluxDBConfig {

    private String host;
    private String token;
    private String database;

    @Bean
    public InfluxDBClient influxDBClient() {
        // 移除token中的"Bearer "前缀（如果存在）
        String cleanToken = token;
        if (token.startsWith("Bearer ")) {
            cleanToken = token.substring(7);
        }
        
        return InfluxDBClient.getInstance(host, cleanToken.toCharArray(), database);
    }
} 