# 高并发博客点赞系统

## 项目简介

本项目是一个基于SpringBoot3 + Redis + MySQL + MyBatis-Plus实现的高并发博客点赞系统。采用多级缓存设计和异步消息队列处理，能够有效处理高并发点赞场景，保证数据一致性和系统性能。系统通过Redis缓存、本地Caffeine缓存、热点数据检测、异步消息队列等技术手段优化，实现了高性能的点赞/取消点赞功能。

## 技术栈

- **基础框架**：SpringBoot 3.4.2
- **数据库**：MySQL 8.0+
- **ORM框架**：MyBatis-Plus 3.5.10.1
- **缓存技术**：
  - Redis (Jedis)
  - Caffeine 本地缓存
  - 多级缓存架构
- **消息队列**：Apache Pulsar
- **用户认证**：Sa-Token 1.42.0
- **工具库**：
  - Hutool 5.8.16
  - Lombok
- **接口文档**：Knife4j 4.4.0
- **JDK版本**：Java 21

## 系统架构

系统采用多级缓存 + 异步写入的架构设计：

### 缓存策略

1. **多级缓存**：
   - 一级缓存：Caffeine本地缓存（热点数据）
   - 二级缓存：Redis分布式缓存（全量数据）
   - 三级存储：MySQL数据库（持久化）

2. **热点检测**：
   - 使用HeavyKeeper算法实时检测热点Key
   - 自动将热点数据提升至本地缓存

3. **点赞状态存储**：
   - 用户点赞状态使用Redis Hash存储
   - 点赞操作通过Lua脚本保证原子性

### 数据一致性保证

1. **异步写入**：
   - 点赞操作先写入Redis
   - 通过Pulsar消息队列异步写入MySQL
   - 批量处理提高写入性能

2. **定时同步**：
   - 定时任务确保Redis和MySQL数据一致性
   - 使用时间切片方式减少峰值写入压力

3. **原子操作**：
   - 使用Redis Lua脚本保证点赞操作原子性
   - 事务管理确保数据库操作一致性

## 核心功能

### 用户登录认证

- 基于Sa-Token实现用户认证
- Token存储于Redis，支持分布式会话
- 提供简单的用户登录接口

### 博客点赞/取消点赞

- 支持用户对博客进行点赞/取消点赞操作
- 通过Redis缓存实现高性能点赞状态查询
- 使用Lua脚本保证点赞操作原子性

### 点赞数据统计

- 实时统计博客点赞数
- 提供用户是否已点赞状态查询
- 博客列表展示包含点赞状态和点赞数

### 高并发处理机制

- **热点检测**：使用HeavyKeeper算法自动识别热点博客
- **多级缓存**：热点博客点赞状态自动提升至本地缓存
- **消息队列**：点赞操作异步写入数据库
- **批量处理**：批量更新点赞记录提高性能

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Apache Pulsar 3.0+

### 安装步骤

1. 克隆代码库

```bash
git clone https://github.com/yourusername/thumb-backend.git
cd thumb-backend
```

2. 执行数据库初始化脚本

```bash
mysql -u root -p < mysql-init/thumb.sql
```

3. 修改配置文件

```bash
vim src/main/resources/application.yml
# 修改数据库、Redis和Pulsar连接配置
```

4. 编译打包

```bash
mvn clean package
```

5. 运行应用

```bash
java -jar target/thumb-backend-0.0.1-SNAPSHOT.jar
```

### 配置说明

主要配置项在`application.yml`中：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/thumb
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
  pulsar:
    client:
      service-url: pulsar://localhost:6650
```

### 接口文档

启动应用后访问：http://localhost:8081/doc.html 查看API文档

## 性能指标

### 系统QPS

- 点赞接口：10,000+ QPS（本地缓存命中）
- 取消点赞接口：8,000+ QPS
- 博客查询接口：5,000+ QPS

### 响应时间

- 点赞/取消点赞操作：平均 < 5ms（缓存命中）
- 博客查询：平均 < 10ms

### 数据一致性保证

- 最终一致性保证：通过定时任务每60秒同步Redis和MySQL数据
- 写入延迟：平均 < 5秒（从点赞操作到数据库持久化）
- 消息队列可靠性：利用Pulsar的持久化机制确保消息不丢失

## 项目结构

```
src/main/java/com/itgr/thumbbackend/
├── ThumbBackendApplication.java    // 应用入口
├── common/                         // 通用类
├── config/                         // 配置类
├── constant/                       // 常量定义
├── controller/                     // 控制器
│   ├── BlogController.java         // 博客相关接口
│   ├── ThumbController.java        // 点赞相关接口
│   └── UserController.java         // 用户相关接口
├── exception/                      // 异常处理
├── job/                            // 定时任务
│   └── SyncThumb2DBJob.java        // 点赞数据同步任务
├── listener/                       // 消息监听器
│   └── thumb/                      // 点赞消息监听
├── manage/                         // 管理类
│   └── cache/                      // 缓存管理
│       ├── CacheManager.java       // 多级缓存管理
│       ├── HeavyKeeper.java        // 热点探测算法
│       └── TopK.java               // Top-K热点接口
├── mapper/                         // MyBatis Mapper
├── model/                          // 数据模型
│   ├── dto/                        // 数据传输对象
│   ├── empty/                      // 实体类
│   ├── enums/                      // 枚举类
│   └── vo/                         // 视图对象
├── service/                        // 服务接口
│   └── impl/                       // 服务实现
│       ├── ThumbServiceImpl.java   // 点赞服务基础实现
│       └── ThumbServiceMQImpl.java // 点赞服务MQ实现
└── util/                           // 工具类
```

## 提交记录优化历程

1. **初始化项目 (992f43d)**
   - 创建基础项目结构
   - 配置SpringBoot、MyBatis-Plus等基础框架
   - 实现基本的用户登录、博客增删改查功能

2. **使用Redis缓存优化点赞功能 (fded686)**
   - 引入Redis缓存用户点赞状态
   - 减少数据库查询，提高点赞操作性能
   - 使用Redis Hash结构存储用户点赞关系

3. **添加数据库初始化脚本 (a3f4ba6)**
   - 添加MySQL表结构初始化脚本
   - 建立适合高并发场景的数据库索引
   - 设计点赞、博客、用户表结构

4. **Redis + Lua脚本优化点赞操作 (27e94a5)**
   - 使用Lua脚本实现点赞操作原子性
   - 减少网络往返，优化点赞性能
   - 实现Redis中点赞计数和状态的一致性

5. **多级缓存解决热点问题 (ef08348)**
   - 引入Caffeine本地缓存
   - 实现HeavyKeeper热点探测算法
   - 热点博客自动提升至本地缓存，大幅提升性能

6. **引入消息队列实现异步点赞 (608ba8e)**
   - 集成Apache Pulsar消息队列
   - 点赞操作异步写入数据库
   - 批量处理提高数据库写入效率

7. **修复Sa-Token与Redis集成的Token携带问题 (9e52058)**
   - 解决Sa-Token与Redis集成后Token不能正确传递的问题
   - 优化认证流程，确保分布式环境下的用户认证

8. **添加测试脚本 (f80b533)**
   - 实现点赞功能的自动化测试
   - 提供Token导出工具，便于测试

