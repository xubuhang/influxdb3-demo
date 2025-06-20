package com.example.influxdb3demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementData {
    
    private String measurement;
    private Map<String, String> tags;
    private Map<String, Object> fields;
    private Instant timestamp;
    
    // 常用的测量类型
    public static class MeasurementTypes {
        public static final String CPU_USAGE = "cpu_usage";
        public static final String MEMORY_USAGE = "memory_usage";
        public static final String DISK_USAGE = "disk_usage";
        public static final String NETWORK_TRAFFIC = "network_traffic";
        public static final String TEMPERATURE = "temperature";
        public static final String HUMIDITY = "humidity";
    }
    
    // 常用的标签键
    public static class TagKeys {
        public static final String HOST = "host";
        public static final String DEVICE = "device";
        public static final String LOCATION = "location";
        public static final String SENSOR_ID = "sensor_id";
    }
    
    // 常用的字段键
    public static class FieldKeys {
        public static final String VALUE = "value";
        public static final String PERCENTAGE = "percentage";
        public static final String BYTES_IN = "bytes_in";
        public static final String BYTES_OUT = "bytes_out";
        public static final String CELSIUS = "celsius";
        public static final String FAHRENHEIT = "fahrenheit";
    }
} 