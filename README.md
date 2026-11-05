# Mall 商城

一个基于 Spring Boot 3 + Vue 3 的前后端分离商城项目，包含前台商城端和后台管理端，覆盖商品、购物车、订单、支付、优惠券、秒杀、会员和 RBAC 权限等完整业务链路。

> 本项目为个人独立开发的前后端实践项目，用于学习和展示完整电商业务落地，非生产环境部署。

## 功能概览

### 前台商城

- 首页聚合：轮播广告、秒杀场次、推荐品牌、新品、人气商品
- 商品列表与搜索：关键字、品牌、分类、排序，基于 MySQL 查询
- 商品详情：SKU 规格、商品属性、促销价格、可用优惠券
- 购物车：同一商品同一 SKU 数量累加，支持改数量、改规格、删除、清空
- 结算下单：收货地址、优惠券、积分抵扣、库存锁定、订单明细
- 支付：模拟支付宝收银台与异步回调
- 订单：列表、详情、取消、确认收货、删除、退货申请
- 会员中心：注册登录、收货地址、优惠券、收藏、品牌关注、浏览足迹

### 后台管理

- 仪表盘：商品、订单、会员、销售数据统计
- 商品管理：商品、分类、品牌、属性、SKU 库存
- 订单管理：查询、详情、发货、关闭、删除、修改收货人/费用/备注
- 售后管理：退货申请审核、退货原因维护
- 订单设置：未支付超时、自动收货天数、自动好评天数
- 营销管理：优惠券、秒杀活动/场次/商品、首页广告与推荐位
- 内容管理：专题、优选专区
- 权限管理：管理员、角色、菜单、接口资源（RBAC）
- 会员管理：会员列表、会员等级
- 文件上传：本地文件存储

## 技术栈

| 分类 | 技术 |
|---|---|
| 后端 | Spring Boot 3.3.4、Java 17、MyBatis-Plus 3.5.7 |
| 前端 | Vue 3.5、Vue Router 4、Vite 5、Element Plus 2.8 |
| 数据库 | MySQL 8 |
| 缓存 | Redis 7 |
| 消息队列 | RabbitMQ 3.13（TTL + 死信队列） |
| 鉴权 | Redis Token 会话 + 拦截器 + RBAC 资源校验 |
| 构建 | Maven Wrapper、npm |

## 核心实现

### 1. 订单 30 分钟超时自动取消

- 下单事务提交后发送延迟消息，避免事务回滚后产生无效消息。
- 消息发送到 `mall.order.delay.exchange`，路由到 `mall.order.delay.queue`。
- 延迟队列配置死信交换机 `mall.order.dlx.exchange`，消息过期后进入 `mall.order.cancel.queue`。
- TTL 通过消息属性设置，默认读取订单设置中的未支付超时时间，初始值为 1800 秒（30 分钟）。
- 消费者收到消息后执行订单取消，并回滚 SKU 库存、优惠券和积分。

### 2. 消费端幂等

- 订单取消使用条件更新：`UPDATE oms_order SET status = 4 WHERE id = ? AND status = 0`。
- 只有更新成功的线程才执行库存、优惠券、积分回滚。
- 重复投递、已支付订单、已关闭订单会更新失败并被安全忽略。
- 支付回调同样使用状态条件更新，避免重复回调重复加积分。

### 3. 商品聚合保存

创建商品时一次写入 8 张表：

| 表 | 内容 |
|---|---|
| `pms_product` | 商品主信息 |
| `pms_member_price` | 会员价 |
| `pms_product_ladder` | 阶梯价 |
| `pms_product_full_reduction` | 满减规则 |
| `pms_sku_stock` | SKU 库存与价格 |
| `pms_product_attribute_value` | 商品参数与规格值 |
| `cms_subject_product_relation` | 专题关联 |
| `cms_prefrence_area_product_relation` | 优选专区关联 |

更新商品采用“先删子表、再全量重插”的方式，SKU 编码按 `yyyyMMdd + 商品id + 3位序号` 生成。

### 4. RBAC 权限

- `ums_menu` 控制后台菜单可见性。
- `ums_resource` 保存接口 URL 与请求方式，控制接口是否可调用。
- 登录后按角色查询菜单与资源，前端控制导航，后端拦截器校验接口权限。

### 5. 购物车实时计价

购物车只保存会员、商品、SKU 和数量，不保存价格。每次查询实时计算会员价、满减、阶梯价等促销信息，避免商品调价后购物车价格过期。

## 项目结构

```text
mall/
├── mall-backend/                 # Spring Boot 后端，同时提供后台与前台接口
│   └── src/main/java/com/macro/mall/
│       ├── controller/admin/     # 后台管理接口
│       ├── controller/portal/    # 前台商城接口
│       ├── service/              # 业务逻辑
│       ├── mapper/               # MyBatis-Plus Mapper
│       ├── entity/               # 数据库实体
│       ├── mq/                   # 订单延迟消息与超时消费
│       ├── auth/                 # Token 与权限拦截
│       └── config/               # RabbitMQ、Web、文件等配置
├── mall-frontend/                # Vue 3 前端
│   └── src/views/
│       ├── mall/                 # 首页、商品、购物车、结算
│       ├── member/               # 会员中心
│       └── admin/                # 后台管理页面
├── sql/mall.sql                  # 表结构、初始化数据与演示账号
├── docker-compose.yml            # MySQL、Redis、RabbitMQ
└── README.md
```

## 快速启动

### 环境要求

- JDK 17
- Node.js 18+
- Docker Desktop（或本机已有 MySQL 8、Redis 7、RabbitMQ 3.13）

### 1. 启动基础设施

```bash
docker compose up -d
```

| 服务 | 地址 | 默认账号 |
|---|---|---|
| MySQL | `localhost:3306` | `root / root`，数据库 `mall_vue` |
| Redis | `localhost:6379` | 无密码 |
| RabbitMQ | `localhost:5672` | `guest / guest` |
| RabbitMQ 管理台 | http://localhost:15672 | `guest / guest` |

MySQL 首次启动会自动执行 `sql/mall.sql` 完成建库、建表和演示数据导入。

### 2. 启动后端

Windows：

```powershell
cd mall-backend
.\mvnw.cmd spring-boot:run
```

macOS / Linux：

```bash
cd mall-backend
./mvnw spring-boot:run
```

后端默认端口 `8080`。

### 3. 启动前端

```bash
cd mall-frontend
npm install
npm run dev
```

### 4. 访问地址

| 端 | 地址 |
|---|---|
| 前台商城 | http://localhost:5173 |
| 后台管理 | http://localhost:5173/admin/login |
| 后端接口 | http://localhost:8080 |

### 演示账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 管理员 | `admin` | `123456` |
| 运营人员 | `operator` | `123456` |
| 会员 | `13800138000` | `123456` |

## 可配置项

| 环境变量 | 说明 | 默认值 |
|---|---|---|
| `MYSQL_URL` | JDBC 连接地址 | `jdbc:mysql://localhost:3306/mall_vue...` |
| `MYSQL_USERNAME` | 数据库用户名 | `root` |
| `MYSQL_PASSWORD` | 数据库密码 | `root` |
| `REDIS_HOST` / `REDIS_PORT` | Redis 地址 | `localhost:6379` |
| `RABBITMQ_HOST` / `RABBITMQ_PORT` | RabbitMQ 地址 | `localhost:5672` |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | RabbitMQ 账号 | `guest / guest` |

订单默认超时时间可在 `application.yml` 的 `mall.order.timeout-minutes` 修改，也可在后台“订单设置”中调整，单位为秒。

## 说明与限制

- 支付宝支付为模拟实现，未接入真实支付宝 SDK；订单状态流转与回调幂等逻辑已保留。
- 商品搜索使用 MySQL `LIKE`，未接入 Elasticsearch。
- RabbitMQ 不可用时下单主流程仍可继续，但订单不会自动超时取消，可手动取消。
- 项目为个人实践项目，演示账号和默认密码仅用于本地运行，公网部署前请修改。
