package com.example.influxdb3demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryResult {
    
    private String measurement;
    private Instant timestamp;
    private Map<String, String> tags;
    private Map<String, Object> fields;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryResponse {
        private List<QueryResult> results;
        private int totalCount;
        private boolean success;
        private String error;
    }
} 