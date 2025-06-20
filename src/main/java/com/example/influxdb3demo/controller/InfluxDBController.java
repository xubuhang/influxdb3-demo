package com.example.influxdb3demo.controller;

import com.example.influxdb3demo.model.MeasurementData;
import com.example.influxdb3demo.model.QueryResult;
import com.example.influxdb3demo.service.InfluxDBService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class InfluxDBController {

    private final InfluxDBService influxDBService;

    @Value("${influxdb.database}")
    private String database;

    /**
     * 主页
     */
    @GetMapping("/")
    public String index(Model model) {
        try {
            List<String> measurements = influxDBService.getMeasurements();
            log.info("主页获取到测量名称列表: {}", measurements);
            model.addAttribute("measurements", measurements);
        } catch (Exception e) {
            log.error("主页获取测量名称失败", e);
            model.addAttribute("measurements", List.of());
        }
        return "index";
    }

    /**
     * 数据查询页面
     */
    @GetMapping("/query")
    public String queryPage(Model model) {
        try {
            List<String> measurements = influxDBService.getMeasurements();
            log.info("获取到测量名称列表: {}", measurements);
            model.addAttribute("measurements", measurements);
        } catch (Exception e) {
            log.error("获取测量名称失败", e);
            // 即使出错也提供一些默认选项
            model.addAttribute("measurements", List.of());
        }
        return "query";
    }

    /**
     * 批量数据生成页面
     */
    @GetMapping("/generate")
    public String generatePage(Model model) {
        try {
            List<String> measurements = influxDBService.getMeasurements();
            log.info("生成页面获取到测量名称列表: {}", measurements);
            model.addAttribute("measurements", measurements);
        } catch (Exception e) {
            log.error("生成页面获取测量名称失败", e);
            model.addAttribute("measurements", List.of());
        }
        return "generate";
    }

    /**
     * 写入单个数据点
     */
    @PostMapping("/api/write")
    @ResponseBody
    public Map<String, Object> writeData(@RequestBody MeasurementData data) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            influxDBService.writeDataPoint(data);
            response.put("success", true);
            response.put("message", "数据写入成功");
        } catch (Exception e) {
            log.error("写入数据失败", e);
            response.put("success", false);
            response.put("message", "数据写入失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 批量写入数据
     */
    @PostMapping("/api/write-batch")
    @ResponseBody
    public Map<String, Object> writeBatchData(@RequestBody List<MeasurementData> dataList) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            influxDBService.writeBatchDataPoints(dataList);
            response.put("success", true);
            response.put("message", "批量数据写入成功，共写入 " + dataList.size() + " 条记录");
        } catch (Exception e) {
            log.error("批量写入数据失败", e);
            response.put("success", false);
            response.put("message", "批量数据写入失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 查询数据
     */
    @PostMapping("/api/query")
    @ResponseBody
    public QueryResult.QueryResponse queryData(
            @RequestParam String measurement,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "100") int limit,
            HttpServletRequest request) {
        
        Instant start = startTime.atZone(ZoneId.systemDefault()).toInstant();
        Instant end = endTime.atZone(ZoneId.systemDefault()).toInstant();
        
        // 手动构建tags Map
        Map<String, String> tags = new HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("tags.") && entry.getValue().length > 0) {
                String tagKey = key.substring(5); // 移除 "tags." 前缀
                String tagValue = entry.getValue()[0];
                if (tagKey != null && !tagKey.isEmpty() && tagValue != null && !tagValue.isEmpty()) {
                    tags.put(tagKey, tagValue);
                }
            }
        }
        
        return influxDBService.queryData(measurement, start, end, tags, limit);
    }

    /**
     * 查询数据为点
     */
    @PostMapping("/api/query-points")
    @ResponseBody
    public QueryResult.QueryResponse queryDataAsPoints(
            @RequestParam String measurement,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "100") int limit,
            HttpServletRequest request) {
        
        Instant start = startTime.atZone(ZoneId.systemDefault()).toInstant();
        Instant end = endTime.atZone(ZoneId.systemDefault()).toInstant();
        
        // 手动构建tags Map
        Map<String, String> tags = new HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("tags.") && entry.getValue().length > 0) {
                String tagKey = key.substring(5); // 移除 "tags." 前缀
                String tagValue = entry.getValue()[0];
                if (tagKey != null && !tagKey.isEmpty() && tagValue != null && !tagValue.isEmpty()) {
                    tags.put(tagKey, tagValue);
                }
            }
        }
        
        return influxDBService.queryDataAsPoints(measurement, start, end, tags, limit);
    }

    /**
     * 批量生成数据
     */
    @PostMapping("/api/generate")
    @ResponseBody
    public Map<String, Object> generateData(
            @RequestParam String measurement,
            @RequestParam(required = false) Map<String, String> tags,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "100") int dataPoints,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Instant start = startTime.atZone(ZoneId.systemDefault()).toInstant();
            Instant end = endTime.atZone(ZoneId.systemDefault()).toInstant();
            
            // 手动解析 baseFields 参数，避免 Spring 自动映射的问题
            Map<String, Object> baseFields = new HashMap<>();
            Map<String, String[]> parameterMap = request.getParameterMap();
            
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("baseFields[") && key.endsWith("]") && entry.getValue().length > 0) {
                    // 提取字段名，去掉 baseFields[ 和 ]
                    String fieldName = key.substring(12, key.length() - 1);
                    String value = entry.getValue()[0];
                    
                    if (fieldName != null && !fieldName.isEmpty() && value != null && !value.isEmpty()) {
                        // 尝试转换为数字类型
                        try {
                            if (value.contains(".")) {
                                // 尝试转换为 Double
                                baseFields.put(fieldName, Double.parseDouble(value));
                            } else {
                                // 尝试转换为 Long
                                baseFields.put(fieldName, Long.parseLong(value));
                            }
                        } catch (NumberFormatException e) {
                            // 如果转换失败，保持为字符串
                            baseFields.put(fieldName, value);
                        }
                    }
                }
            }
            
            log.info("解析的字段: {}", baseFields);
            
            List<MeasurementData> mockData = influxDBService.generateMockData(
                    measurement, tags, baseFields, start, end, dataPoints);
            
            influxDBService.writeBatchDataPoints(mockData);
            
            response.put("success", true);
            response.put("message", "成功生成并写入 " + dataPoints + " 条模拟数据");
            response.put("dataCount", dataPoints);
        } catch (Exception e) {
            log.error("生成数据失败", e);
            response.put("success", false);
            response.put("message", "生成数据失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 获取测量列表
     */
    @GetMapping("/api/measurements")
    @ResponseBody
    public Map<String, Object> getMeasurements() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> measurements = influxDBService.getMeasurements();
            response.put("success", true);
            response.put("measurements", measurements);
        } catch (Exception e) {
            log.error("获取测量名称失败", e);
            response.put("success", false);
            response.put("message", "获取测量名称失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 生成示例数据
     */
    @PostMapping("/api/generate-sample")
    @ResponseBody
    public Map<String, Object> generateSampleData(@RequestParam String type) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String measurement;
            Map<String, String> tags = new HashMap<>();
            Map<String, Object> baseFields = new HashMap<>();
            
            switch (type) {
                case "cpu":
                    measurement = "cpu_usage";
                    tags.put("host", "server-01");
                    tags.put("cpu", "cpu0");
                    baseFields.put("usage_percent", 50.0);
                    baseFields.put("load_average", 2.5);
                    break;
                case "memory":
                    measurement = "memory_usage";
                    tags.put("host", "server-01");
                    tags.put("type", "ram");
                    baseFields.put("usage_percent", 70.0);
                    baseFields.put("total_gb", 16.0);
                    baseFields.put("used_gb", 11.2);
                    break;
                case "temperature":
                    measurement = "temperature";
                    tags.put("location", "room-101");
                    tags.put("sensor_id", "temp-001");
                    baseFields.put("celsius", 22.5);
                    baseFields.put("fahrenheit", 72.5);
                    baseFields.put("humidity", 45.0);
                    break;
                default:
                    response.put("success", false);
                    response.put("message", "不支持的示例类型: " + type);
                    return response;
            }
            
            // 生成最近1小时的数据
            Instant endTime = Instant.now();
            Instant startTime = endTime.minus(1, java.time.temporal.ChronoUnit.HOURS);
            
            List<MeasurementData> mockData = influxDBService.generateMockData(
                    measurement, tags, baseFields, startTime, endTime, 100);
            
            influxDBService.writeBatchDataPoints(mockData);
            
            response.put("success", true);
            response.put("message", "成功生成 " + type + " 示例数据，共 " + mockData.size() + " 条记录");
            response.put("dataCount", mockData.size());
            response.put("measurement", measurement);
            
        } catch (Exception e) {
            log.error("生成示例数据失败", e);
            response.put("success", false);
            response.put("message", "生成示例数据失败: " + e.getMessage());
        }
        
        return response;
    }

    /**
     * 刷新测量列表
     */
    @GetMapping("/api/refresh-measurements")
    @ResponseBody
    public Map<String, Object> refreshMeasurements() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> measurements = influxDBService.getMeasurements();
            log.info("刷新测量列表成功: {}", measurements);
            response.put("success", true);
            response.put("measurements", measurements);
            response.put("count", measurements.size());
        } catch (Exception e) {
            log.error("刷新测量列表失败", e);
            response.put("success", false);
            response.put("message", "刷新测量列表失败: " + e.getMessage());
        }
        
        return response;
    }
} 