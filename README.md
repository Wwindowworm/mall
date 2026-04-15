# 优选网购 · Premium Mall

基于 Spring Boot 3.2 + MyBatis-Plus 的电商系统，支持商品浏览、AI 推荐、秒杀、购物车、订单、签到积分、RabbitMQ 延迟队列等完整电商能力。

---

## 🛠 技术栈

| 层级 | 技术 |
|------|------|
| 框架 | Spring Boot 3.2.5 |
| ORM | MyBatis-Plus 3.5.6 |
| 缓存 | Redis（Spring Data Redis） |
| 消息队列 | RabbitMQ 3.13 + Spring AMQP |
| 数据库 | MySQL 8.x |
| AI | DeepSeek API（商品智能推荐） |
| 认证 | JWT（jjwt 0.12） |
| 前端 | 原生 HTML/CSS/JS（无框架依赖） |
| 构建 | Maven 3.9 |

---

## ✨ 功能模块

- **用户**：邮箱验证码登录/注册（QQ Mail SMTP）
- **商品**：分类浏览、商品详情、AI 智能推荐（DeepSeek V3）
- **秒杀**：限时抢购 + Redis 限流 + RabbitMQ 延迟队列超时自动关单
- **购物车**：增删改查、数量合并
- **订单**：创建、列表、取消、模拟支付、确认收货
- **收藏**：收藏/取消收藏商品
- **收货地址**：增删改查、设默认
- **签到**：每日签到积分体系

---

## 🚀 快速启动

### 环境要求

- JDK 17+
- MySQL 8.x
- Redis 3.x+
- RabbitMQ 3.x（可选，不影响核心功能）
- Maven 3.9+

### 步骤

**1. 初始化数据库**

```sql
mysql -u root -p mall < sql/init.sql
mysql -u root -p mall < sql/init_new_features.sql
```

**2. 配置数据库连接**

编辑 `src/main/resources/application.yml`，修改以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mall?useUnicode=true&characterEncoding=utf-8
    username: root
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
  rabbitmq:
    username: guest
    password: guest
  mail:
    host: smtp.qq.com
    username: your_qq@qq.com
    password: your_smtp_password
jwt:
  secret: your-jwt-secret-key-must-be-at-least-32-bytes
  expiration: 86400000
```

**3. 启动**

```bash
mvn clean package -DskipTests
java -jar target/mall-1.0.0.jar
```

访问：**http://localhost:8080/index.html**

---

## 📡 API 概览

| 接口 | 方法 | 说明 | 鉴权 |
|------|------|------|------|
| `/api/user/sendCode` | POST | 发送邮箱验证码 | ❌ |
| `/api/user/login` | POST | 登录（自动注册新用户） | ❌ |
| `/api/category/list` | GET | 分类列表 | ❌ |
| `/api/product/list` | GET | 商品列表（分页） | ❌ |
| `/api/product/detail/{id}` | GET | 商品详情 | ❌ |
| `/api/product/recommend` | GET | AI 推荐商品 | ❌ |
| `/api/seckill/list` | GET | 秒杀活动列表 | ❌ |
| `/api/sign/in` | POST | 签到 | ✅ |
| `/api/cart/*` | * | 购物车 CRUD | ✅ |
| `/api/favorite/*` | * | 收藏 | ✅ |
| `/api/address/*` | * | 收货地址 | ✅ |
| `/api/order/*` | * | 订单管理 | ✅ |

> ✅ = 需要 Header：`Authorization: Bearer <token>`
> 登录成功后返回的 token

---

## 📁 项目结构

```
src/main/java/com/mall/
├── MallApplication.java          # 启动入口
├── config/                      # 配置类
│   ├── WebConfig.java           # CORS + 拦截器
│   ├── JwtConfig.java           # JWT 工具
│   ├── JwtInterceptor.java      # 鉴权拦截器
│   ├── CorsFilter.java          # 全局跨域过滤器
│   ├── RabbitMQConfig.java      # MQ 队列配置
│   ├── DeepSeekConfig.java      # DeepSeek AI
│   └── RedisConfig.java
├── controller/                  # 控制器
│   ├── UserController.java      # 登录/注册
│   ├── ProductController.java   # 商品
│   ├── SeckillController.java   # 秒杀
│   ├── CartController.java      # 购物车
│   ├── OrderController.java     # 订单
│   ├── AddressController.java   # 收货地址
│   ├── FavoriteController.java  # 收藏
│   ├── SignController.java      # 签到
│   └── DebugController.java    # 调试工具
├── entity/                     # 实体类
├── mapper/                     # MyBatis Mapper
├── service/                    # 业务逻辑
├── dto/                        # 数据传输对象
└── common/                    # 通用类（Result, BizException）

src/main/resources/
├── application.yml             # 主配置
└── static/
    └── index.html              # 前端页面

sql/
├── init.sql                    # 核心表（用户、商品、秒杀、签到）
└── init_new_features.sql       # 扩展表（购物车、订单、收藏等）
```

---

## ⚙️ 关键配置说明

### JWT
- 密钥：`jwt.secret`（默认 32 字节即可）
- 有效期：`jwt.expiration`（毫秒，默认 86400000 = 1 天）

### Redis
- 默认无密码，如需密码：`spring.data.redis.password`
- Redisson 已禁用，使用原生 Spring Data Redis

### RabbitMQ
- 默认 guest/guest
- 延迟队列 TTL：30 秒（超时自动关单）
- 库存同步队列：订单创建时扣减库存

### 邮件
- 使用 QQ 邮箱 SMTP（无需第三方 API，免费）
- `mail.from`：发件人显示名称
- 验证码 5 分钟有效，同 IP 限制 60 秒内最多 5 次

### DeepSeek AI
- 模型：`deepseek-chat`
- API Key：`https://platform.deepseek.com/` 免费额度
- 配置：`spring.ai.deepseek.api-key`

---

## 📝 数据库表

| 表名 | 说明 |
|------|------|
| t_user | 用户 |
| t_product | 商品 |
| t_product_review | 商品评价 |
| t_seckill_activity | 秒杀活动 |
| t_seckill_order | 秒杀订单 |
| t_sign_record | 签到记录 |
| t_cart | 购物车 |
| t_order | 订单 |
| t_order_item | 订单明细 |
| t_address | 收货地址 |
| t_category | 商品分类 |
| t_favorite | 收藏 |

---

## 🔧 常见问题

**Q: 商品加载失败？**
> 检查后端是否启动（http://localhost:8080/api/category/list 返回 JSON）

**Q: 登录失败？**
> 检查 Redis 是否启动（存储验证码）；检查 QQ 邮箱 SMTP 密码是否配置正确

**Q: 秒杀/订单超时未自动关闭？**
> 需要启动 RabbitMQ，延迟队列依赖 MQ

**Q: AI 推荐不工作？**
> 配置 `spring.ai.deepseek.api-key`，或查看日志中 `DeepSeek` 相关输出

---

> 本项目仅供学习参考，生产环境请更换强密码、配置 HTTPS、启用 Redis 密码、接入真实支付接口。
