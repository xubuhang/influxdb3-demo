# InfluxDB v3 演示应用

这是一个基于Spring Boot的InfluxDB v3读写演示应用，提供了完整的Web界面来管理InfluxDB数据。

## 功能特性

- 🔍 **数据查询**: 支持按时间范围、测量名称和标签进行灵活查询
- 📊 **数据生成**: 批量生成模拟数据，支持自定义测量、标签和字段
- 🌐 **Web界面**: 现代化的响应式Web界面，支持实时数据展示
- ⚡ **高性能**: 基于InfluxDB v3的高性能时序数据库
- 🎯 **易用性**: 直观的操作界面，支持模板快速生成数据

## 技术栈

- **后端**: Spring Boot 3.2.0
- **数据库**: InfluxDB v3
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

### 4. 运行应用

```bash
# 克隆项目
git clone <repository-url>
cd influxdb3-demo

# 编译项目
mvn clean compile

# 运行应用
mvn spring-boot:run
```

应用将在 `http://localhost:8080` 启动。

### 5. 使用应用

1. **访问主页**: 打开浏览器访问 `http://localhost:8080`
2. **生成示例数据**: 在主页点击快速操作按钮生成CPU、内存或温度数据
3. **查询数据**: 点击"数据查询"菜单，设置查询条件查看数据
4. **批量生成**: 点击"数据生成"菜单，自定义生成更多数据

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
│   │   │   ├── QueryResult.java             # 查询结果模型
│   │   │   └── BatchDataRequest.java        # 批量数据请求模型
│   │   ├── service/
│   │   │   └── InfluxDBService.java         # InfluxDB服务类
│   │   └── Influxdb3DemoApplication.java    # 主应用类
│   └── resources/
│       ├── templates/
│       │   ├── index.html                   # 主页
│       │   ├── query.html                   # 数据查询页面
│       │   └── generate.html                # 数据生成页面
│       └── application.yml                  # 应用配置文件
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
GET /api/query?measurement=cpu_usage&startTime=2024-01-01T00:00:00Z&endTime=2024-01-01T23:59:59Z&limit=100
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

### 常用测量类型

- `cpu_usage`: CPU使用率数据
- `memory_usage`: 内存使用率数据
- `disk_usage`: 磁盘使用率数据
- `network_traffic`: 网络流量数据
- `temperature`: 温度传感器数据
- `humidity`: 湿度传感器数据

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

### 日志查看

应用使用SLF4J进行日志记录，可以在 `application.yml` 中配置日志级别：

```yaml
logging:
  level:
    com.example.influxdb3demo: DEBUG
    com.influxdb: DEBUG
```

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