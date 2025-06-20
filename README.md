# InfluxDB v3 演示应用

这是一个基于Spring Boot的InfluxDB v3读写演示应用，提供了完整的Web界面来管理InfluxDB数据。

## 功能特性

- 🔍 **数据查询**: 支持按时间范围、测量名称和标签进行灵活查询
- 📊 **数据生成**: 批量生成模拟数据，支持自定义测量、标签和字段
- 🌐 **Web界面**: 现代化的响应式Web界面，支持实时数据展示
- ⚡ **高性能**: 基于InfluxDB v3的高性能时序数据库
- 🎯 **易用性**: 直观的操作界面，支持模板快速生成数据
- 🔄 **动态刷新**: 支持动态刷新测量名称列表
- 📋 **智能解析**: 自动解析查询结果中的时间戳、标签和字段

## 技术栈

- **后端**: Spring Boot 3.2.0
- **数据库**: InfluxDB v3 (IOx架构)
- **客户端**: influxdb3-java 1.1.0
- **前端**: Bootstrap 5 + Thymeleaf
- **构建工具**: Maven
- **Java版本**: 17+

## 快速开始

### 1. 环境要求

- Java 17 或更高版本
- Maven 3.6 或更高版本
- InfluxDB v3 实例

### 2. 配置InfluxDB

在运行应用之前，请确保：

1. 安装并启动InfluxDB v3
2. 创建组织和存储桶(Bucket)
3. 生成API Token

### 3. 配置应用

编辑 `src/main/resources/application.yml` 文件，配置InfluxDB连接信息：

```yaml
influxdb:
  url: http://localhost:8086
  token: your-influxdb-token-here
  org: your-organization
  bucket: your-bucket
  database: your-database
```

### 4. 重要：JVM参数配置

由于使用了Apache Arrow库，需要添加特定的JVM参数来避免内存访问错误：

#### 方法1：使用提供的启动脚本

**Windows:**
```bash
start.bat
```

**Linux/Mac:**
```bash
./start.sh
```

#### 方法2：手动添加JVM参数

```bash
java --add-opens=java.base/java.nio=org.apache.arrow.memory.core,ALL-UNNAMED \
     --add-opens=java.base/sun.nio.ch=org.apache.arrow.memory.core,ALL-UNNAMED \
     -jar target/influxdb3-demo-1.0.0.jar
```

#### 方法3：IDE配置

在IntelliJ IDEA中：
1. Run → Edit Configurations
2. 在VM options中添加：
```
--add-opens=java.base/java.nio=org.apache.arrow.memory.core,ALL-UNNAMED
--add-opens=java.base/sun.nio.ch=org.apache.arrow.memory.core,ALL-UNNAMED
```

### 5. 运行应用

```bash
# 克隆项目
git clone <repository-url>
cd influxdb3-demo

# 编译项目
mvn clean compile

# 运行应用（使用启动脚本）
./start.sh  # Linux/Mac
start.bat   # Windows

# 或者直接使用Maven（需要先配置JVM参数）
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

### 6. 使用应用

1. **访问主页**: 打开浏览器访问 `http://localhost:8080`
2. **生成示例数据**: 在主页点击快速操作按钮生成CPU、内存或温度数据
3. **查询数据**: 点击"数据查询"菜单，设置查询条件查看数据
4. **批量生成**: 点击"数据生成"菜单，自定义生成更多数据
5. **刷新测量列表**: 在查询页面点击"刷新测量列表"按钮

## 项目结构

```
src/
├── main/
│   ├── java/com/example/influxdb3demo/
│   │   ├── config/
│   │   │   └── InfluxDBConfig.java          # InfluxDB配置类
│   │   ├── controller/
│   │   │   └── InfluxDBController.java      # 控制器类
│   │   ├── model/
│   │   │   ├── MeasurementData.java         # 测量数据模型
│   │   │   └── QueryResult.java             # 查询结果模型
│   │   ├── service/
│   │   │   └── InfluxDBService.java         # InfluxDB服务类
│   │   └── Influxdb3DemoApplication.java    # 主应用类
│   └── resources/
│       ├── templates/
│       │   ├── index.html                   # 主页
│       │   ├── query.html                   # 数据查询页面
│       │   └── generate.html                # 数据生成页面
│       └── application.yml                  # 应用配置文件
├── start.bat                                # Windows启动脚本
└── start.sh                                 # Linux/Mac启动脚本
```

## API接口

### 数据写入

```http
POST /api/write
Content-Type: application/json

{
  "measurement": "cpu_usage",
  "tags": {
    "host": "server-01",
    "cpu": "cpu0"
  },
  "fields": {
    "usage_percent": 75.5
  },
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### 数据查询

```http
POST /api/query
Content-Type: application/x-www-form-urlencoded

measurement=cpu_usage&startTime=2024-01-01T00:00:00Z&endTime=2024-01-01T23:59:59Z&limit=100
```

### 获取测量名称列表

```http
GET /api/measurements
```

### 批量数据生成

```http
POST /api/generate
Content-Type: application/json

{
  "measurement": "cpu_usage",
  "tags": {
    "host": "server-01"
  },
  "fields": {
    "usage_percent": 50.0
  },
  "startTime": "2024-01-01T00:00:00Z",
  "endTime": "2024-01-01T01:00:00Z",
  "dataPoints": 60
}
```

### 生成示例数据

```http
POST /api/generate-sample?type=cpu
```

## 数据模型

### MeasurementData (测量数据)

```java
{
  "measurement": "cpu_usage",           // 测量名称
  "tags": {                             // 标签键值对
    "host": "server-01",
    "cpu": "cpu0"
  },
  "fields": {                           // 字段键值对
    "usage_percent": 75.5,
    "temperature": 45.2
  },
  "timestamp": "2024-01-01T12:00:00Z"   // 时间戳
}
```

### QueryResult (查询结果)

```java
{
  "timestamp": "2024-01-01T12:00:00Z",  // 时间戳
  "tags": {                             // 标签
    "host": "server-01",
    "cpu": "cpu0"
  },
  "fields": {                           // 字段值
    "usage_percent": 75.5,
    "temperature": 45.2
  }
}
```

### 常用测量类型

- `cpu_usage`: CPU使用率数据
- `memory_usage`: 内存使用率数据
- `disk_usage`: 磁盘使用率数据
- `network_traffic`: 网络流量数据
- `temperature`: 温度传感器数据
- `humidity`: 湿度传感器数据

## InfluxDB v3 特性

### 查询语法

本应用使用InfluxDB v3的SQL语法：

```sql
-- 获取所有表名（测量名称）
SHOW TABLES

-- 查询数据
SELECT * FROM cpu_usage 
WHERE time >= '2024-01-01T00:00:00Z' 
  AND time <= '2024-01-01T23:59:59Z'
ORDER BY time DESC 
LIMIT 100
```

### 架构变化

- 测量名称 = 表名
- 使用SQL语法替代InfluxQL
- 支持标准SQL查询

## 开发指南

### 添加新的测量类型

1. 在 `MeasurementData.java` 中添加新的测量类型常量
2. 在 `InfluxDBController.java` 中添加生成方法
3. 在前端模板中添加相应的按钮和表单

### 自定义查询

修改 `InfluxDBService.java` 中的 `queryData` 方法来支持更复杂的查询条件。

### 扩展数据模型

在 `model` 包中添加新的数据模型类，并在服务层实现相应的业务逻辑。

## 故障排除

### 常见问题

1. **连接失败**: 检查InfluxDB URL和Token配置
2. **权限错误**: 确保API Token有足够的权限
3. **数据格式错误**: 检查时间戳格式和字段类型
4. **JVM参数错误**: 确保添加了Apache Arrow所需的JVM参数
5. **编译错误**: 确保使用Java 17编译，Java 8不支持Spring Boot 3.x

### 错误解决

#### Apache Arrow MemoryUtil 错误
```
java.lang.ExceptionInInitializerError: java.lang.RuntimeException: java.lang.reflect.InaccessibleObjectException
```

**解决方案**: 添加JVM参数
```
--add-opens=java.base/java.nio=org.apache.arrow.memory.core,ALL-UNNAMED
--add-opens=java.base/sun.nio.ch=org.apache.arrow.memory.core,ALL-UNNAMED
```

#### 查询结果解析错误
```
java.time.format.DateTimeParseException: Text 'cpu0' could not be parsed at index 0
```

**解决方案**: 已修复，现在会自动识别字段类型并正确解析

#### 测量名称下拉列表为空
```
Error during planning: table 'public.iox._measurements' not found
```

**解决方案**: 已修复，现在使用正确的 `SHOW TABLES` 语法

### 日志查看

应用使用SLF4J进行日志记录，可以在 `application.yml` 中配置日志级别：

```yaml
logging:
  level:
    com.example.influxdb3demo: DEBUG
    com.influxdb: DEBUG
```

## 更新日志

### v1.0.0 (2024-06-20)
- ✅ 修复查询结果解析错误
- ✅ 修复测量名称获取问题
- ✅ 添加JVM参数配置说明
- ✅ 改进错误处理和日志记录
- ✅ 添加动态刷新测量列表功能
- ✅ 更新API接口为POST请求
- ✅ 优化前端用户体验

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

如有问题或建议，请通过以下方式联系：

- 提交 Issue
- 发送邮件
- 参与讨论

---

**注意**: 这是一个演示应用，生产环境使用前请进行充分的安全性和性能测试。 