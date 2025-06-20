package com.example.influxdb3demo.service;

import com.example.influxdb3demo.model.MeasurementData;
import com.example.influxdb3demo.model.QueryResult;
import com.influxdb.v3.client.InfluxDBClient;
import com.influxdb.v3.client.Point;
import com.influxdb.v3.client.PointValues;
import com.influxdb.v3.client.query.QueryOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class InfluxDBService {

    private final InfluxDBClient influxDBClient;
    
    @Value("${influxdb.database}")
    private String database;

    /**
     * 写入单个数据点
     */
    public void writeDataPoint(MeasurementData data) {
        try {
            Point point = Point.measurement(data.getMeasurement())
                    .setTimestamp(data.getTimestamp());
            
            // 添加标签
            if (data.getTags() != null) {
                data.getTags().forEach(point::setTag);
            }
            
            // 添加字段
            if (data.getFields() != null) {
                data.getFields().forEach((key, value) -> {
                    if (value instanceof Number) {
                        point.setField(key, (Number) value);
                    } else if (value instanceof String) {
                        point.setField(key, (String) value);
                    } else if (value instanceof Boolean) {
                        point.setField(key, (Boolean) value);
                    }
                });
            }
            
            influxDBClient.writePoint(point);
            log.info("成功写入数据点: {}", point);
        } catch (Exception e) {
            log.error("写入数据点失败", e);
            throw new RuntimeException("写入数据点失败", e);
        }
    }

    /**
     * 批量写入数据点
     */
    public void writeBatchDataPoints(List<MeasurementData> dataList) {
        try {
            List<Point> points = new ArrayList<>();
            
            for (MeasurementData data : dataList) {
                Point point = Point.measurement(data.getMeasurement())
                        .setTimestamp(data.getTimestamp());
                
                // 添加标签
                if (data.getTags() != null) {
                    data.getTags().forEach(point::setTag);
                }
                
                // 添加字段
                if (data.getFields() != null) {
                    data.getFields().forEach((key, value) -> {
                        if (value instanceof Number) {
                            point.setField(key, (Number) value);
                        } else if (value instanceof String) {
                            point.setField(key, (String) value);
                        } else if (value instanceof Boolean) {
                            point.setField(key, (Boolean) value);
                        }
                    });
                }
                
                points.add(point);
            }
            
            // 逐个写入点
            for (Point point : points) {
                influxDBClient.writePoint(point);
            }
            log.info("成功批量写入 {} 个数据点", points.size());
        } catch (Exception e) {
            log.error("批量写入数据点失败", e);
            throw new RuntimeException("批量写入数据点失败", e);
        }
    }

    /**
     * 查询数据
     */
    public QueryResult.QueryResponse queryData(String measurement, Instant startTime, Instant endTime, 
                                             Map<String, String> tags, int limit) {
        try {
            StringBuilder sqlQuery = new StringBuilder();
            sqlQuery.append("SELECT * FROM ").append(measurement);
            
            // 添加时间范围
            sqlQuery.append(" WHERE time >= '").append(startTime).append("'");
            sqlQuery.append(" AND time <= '").append(endTime).append("'");
            
            // 添加标签过滤
            if (tags != null && !tags.isEmpty()) {
                for (Map.Entry<String, String> tag : tags.entrySet()) {
                    sqlQuery.append(" AND ").append(tag.getKey()).append(" = '").append(tag.getValue()).append("'");
                }
            }
            
            sqlQuery.append(" ORDER BY time DESC LIMIT ").append(limit);
            
            log.info("执行SQL查询: {}", sqlQuery);
            
            List<QueryResult> results = new ArrayList<>();
            
            try (Stream<Object[]> stream = influxDBClient.query(sqlQuery.toString())) {
                stream.forEach(row -> {
                    QueryResult queryResult = new QueryResult();
                    
                    // 提取字段，避免假设字段顺序
                    Map<String, Object> fields = new HashMap<>();
                    Map<String, String> recordTags = new HashMap<>();
                    Instant timestamp = null;
                    
                    // 遍历所有字段，根据类型进行分类
                    for (int i = 0; i < row.length; i++) {
                        if (row[i] != null) {
                            String fieldName = "field_" + i;
                            Object value = row[i];
                            
                            // 检查是否为时间戳
                            if (value instanceof Instant) {
                                timestamp = (Instant) value;
                                log.debug("找到时间戳字段: {}", timestamp);
                            } else if (value instanceof String) {
                                String strValue = (String) value;
                                // 尝试解析为时间戳
                                try {
                                    timestamp = Instant.parse(strValue);
                                    log.debug("解析时间戳字符串: {}", timestamp);
                                } catch (DateTimeParseException e) {
                                    // 不是时间戳，可能是标签或字段值
                                    if (strValue.contains("=")) {
                                        // 可能是标签格式
                                        String[] parts = strValue.split("=", 2);
                                        if (parts.length == 2) {
                                            recordTags.put(parts[0], parts[1]);
                                        } else {
                                            fields.put(fieldName, strValue);
                                        }
                                    } else {
                                        fields.put(fieldName, strValue);
                                    }
                                }
                            } else if (value instanceof Number) {
                                // 检查是否为时间戳数字
                                Number numValue = (Number) value;
                                long longValue = numValue.longValue();
                                if (longValue > 1_000_000_000_000L) { // 可能是纳秒时间戳
                                    long seconds = longValue / 1_000_000_000L;
                                    long nanoAdjustment = longValue % 1_000_000_000L;
                                    timestamp = Instant.ofEpochSecond(seconds, nanoAdjustment);
                                    log.debug("转换数字时间戳: {}", timestamp);
                                } else {
                                    fields.put(fieldName, value);
                                }
                            } else {
                                fields.put(fieldName, value);
                            }
                        }
                    }
                    
                    // 设置解析的结果
                    if (timestamp != null) {
                        queryResult.setTimestamp(timestamp);
                    }
                    if (!recordTags.isEmpty()) {
                        queryResult.setTags(recordTags);
                    }
                    if (!fields.isEmpty()) {
                        queryResult.setFields(fields);
                    }
                    
                    results.add(queryResult);
                    log.debug("添加查询结果: timestamp={}, tags={}, fields={}", 
                             timestamp, recordTags, fields);
                });
            }
            
            return QueryResult.QueryResponse.builder()
                    .results(results)
                    .totalCount(results.size())
                    .success(true)
                    .build();
                    
        } catch (Exception e) {
            log.error("查询数据失败", e);
            return QueryResult.QueryResponse.builder()
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * 使用PointValues查询数据
     */
    public QueryResult.QueryResponse queryDataAsPoints(String measurement, Instant startTime, Instant endTime, 
                                                     Map<String, String> tags, int limit) {
        try {
            StringBuilder sqlQuery = new StringBuilder();
            sqlQuery.append("SELECT * FROM ").append(measurement);
            
            // 添加时间范围
            sqlQuery.append(" WHERE time >= '").append(startTime).append("'");
            sqlQuery.append(" AND time <= '").append(endTime).append("'");
            
            // 添加标签过滤
            if (tags != null && !tags.isEmpty()) {
                for (Map.Entry<String, String> tag : tags.entrySet()) {
                    sqlQuery.append(" AND ").append(tag.getKey()).append(" = '").append(tag.getValue()).append("'");
                }
            }
            
            sqlQuery.append(" ORDER BY time DESC LIMIT ").append(limit);
            
            log.info("执行SQL查询: {}", sqlQuery);
            
            List<QueryResult> results = new ArrayList<>();
            
            try (Stream<PointValues> stream = influxDBClient.queryPoints(sqlQuery.toString(), QueryOptions.DEFAULTS)) {
                stream.forEach(point -> {
                    QueryResult queryResult = new QueryResult();
                    
                    // 提取时间戳
                    Number timestampNumber = point.getTimestamp();
                    if (timestampNumber != null) {
                        // 将纳秒时间戳转换为Instant
                        long nanos = timestampNumber.longValue();
                        long seconds = nanos / 1_000_000_000L;
                        long nanoAdjustment = nanos % 1_000_000_000L;
                        Instant timestamp = Instant.ofEpochSecond(seconds, nanoAdjustment);
                        queryResult.setTimestamp(timestamp);
                    }
                    
                    // 提取标签
                    Map<String, String> recordTags = new HashMap<>();
                    for (String tagName : point.getTagNames()) {
                        recordTags.put(tagName, point.getTag(tagName));
                    }
                    queryResult.setTags(recordTags);
                    
                    // 提取字段
                    Map<String, Object> fields = new HashMap<>();
                    for (String fieldName : point.getFieldNames()) {
                        fields.put(fieldName, point.getField(fieldName, Object.class));
                    }
                    queryResult.setFields(fields);
                    
                    results.add(queryResult);
                });
            }
            
            return QueryResult.QueryResponse.builder()
                    .results(results)
                    .totalCount(results.size())
                    .success(true)
                    .build();
                    
        } catch (Exception e) {
            log.error("查询数据失败", e);
            return QueryResult.QueryResponse.builder()
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }

    /**
     * 生成模拟数据
     */
    public List<MeasurementData> generateMockData(String measurement, Map<String, String> tags, 
                                                 Map<String, Object> baseFields, Instant startTime, 
                                                 Instant endTime, int dataPoints) {
        List<MeasurementData> dataList = new ArrayList<>();
        long intervalMillis = Duration.between(startTime, endTime).toMillis() / dataPoints;
        
        for (int i = 0; i < dataPoints; i++) {
            Instant timestamp = startTime.plusMillis(i * intervalMillis);
            Map<String, Object> fields = new HashMap<>(baseFields);
            
            // 为数值字段添加随机变化
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                if (entry.getValue() instanceof Number) {
                    double baseValue = ((Number) entry.getValue()).doubleValue();
                    double variation = baseValue * 0.1 * (ThreadLocalRandom.current().nextDouble() - 0.5);
                    fields.put(entry.getKey(), baseValue + variation);
                }
            }
            
            MeasurementData data = MeasurementData.builder()
                    .measurement(measurement)
                    .tags(tags)
                    .fields(fields)
                    .timestamp(timestamp)
                    .build();
            
            dataList.add(data);
        }
        
        return dataList;
    }

    /**
     * 获取所有测量名称（表名）
     */
    public List<String> getMeasurements() {
        try {
            // 使用 SHOW TABLES 获取所有表，然后过滤出用户表
            String sqlQuery = "SHOW TABLES";
            log.info("执行查询获取表名: {}", sqlQuery);
            
            List<String> measurements = new ArrayList<>();
            
            try (Stream<Object[]> stream = influxDBClient.query(sqlQuery)) {
                stream.forEach(row -> {
                    log.debug("查询结果行: {}", (Object) row);
                    // SHOW TABLES 返回的列：table_catalog, table_schema, table_name, table_type
                    if (row.length >= 3 && row[1] != null && row[2] != null) {
                        String tableSchema = String.valueOf(row[1]);
                        String tableName = String.valueOf(row[2]);
                        
                        // 只获取用户表（table_schema 为 'iox' 的表）
                        if ("iox".equals(tableSchema)) {
                            measurements.add(tableName);
                            log.debug("添加用户表名: {}", tableName);
                        }
                    }
                });
            }
            
            log.info("成功获取到 {} 个用户表名: {}", measurements.size(), measurements);
            return measurements;
        } catch (Exception e) {
            log.error("获取表名失败", e);
            return new ArrayList<>();
        }
    }
} 