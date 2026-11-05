# Mall 商城

这是我自己做的一个前后端分离商城项目，前台购物和后台管理都放在里面。主要想把商品、购物车、下单、支付、订单超时取消这条链路完整跑一遍，顺便熟悉 Spring Boot 和 Vue 的实际用法。

项目是个人练习，不是生产系统。支付用的是模拟支付宝，没有接真实 SDK。

## 截图

| 前台首页 | 商品详情 |
|---|---|
| ![前台首页](https://cdn.jsdmirror.com/gh/DeFaFa/mall@main/docs/screenshots/01-home.png?v=2) | ![商品详情](https://cdn.jsdmirror.com/gh/DeFaFa/mall@main/docs/screenshots/02-product.png?v=2) |

| 购物车 | 确认订单 |
|---|---|
| ![购物车](https://cdn.jsdmirror.com/gh/DeFaFa/mall@main/docs/screenshots/03-cart.png?v=2) | ![确认订单](https://cdn.jsdmirror.com/gh/DeFaFa/mall@main/docs/screenshots/04-checkout.png?v=2) |

| 后台仪表盘 | 商品管理 |
|---|---|
| ![后台仪表盘](https://cdn.jsdmirror.com/gh/DeFaFa/mall@main/docs/screenshots/05-admin-dashboard.png?v=2) | ![商品管理](https://cdn.jsdmirror.com/gh/DeFaFa/mall@main/docs/screenshots/06-admin-products.png?v=2) |

## 功能

前台这边，首页会聚合轮播图、秒杀场次、推荐品牌、新品和人气商品；商品列表可以按关键字、品牌、分类和排序查；详情页有 SKU、参数、促销价和可领优惠券；购物车支持改数量、改规格、删除；下单会校验地址、库存、优惠券和积分，然后锁库存、生成订单、删除购物车。

会员中心包括注册登录、收货地址、我的订单、优惠券、收藏、品牌关注和浏览足迹。订单支持取消、确认收货、删除和申请退货。

后台主要是商品、订单、营销和权限四块。商品部分维护商品、分类、品牌、属性、SKU 库存；订单部分可以查询、看详情、发货、关闭、删除、改收货人/费用/备注，也能审核退货；营销部分管优惠券、秒杀活动和首页推荐位；权限部分用 RBAC 管理管理员、角色、菜单和接口资源。另外还有专题、优选专区、会员等级和文件上传。

功能不逐条展开了，下面写几个实现时花时间比较多的点。

## 技术栈

后端用 Spring Boot 3.3 + Java 17 + MyBatis-Plus，前端用 Vue 3 + Vite + Element Plus，数据存 MySQL 8，登录态放 Redis 7，订单超时用 RabbitMQ 3.13 的 TTL 和死信队列。后台接口用拦截器做权限校验。

## 几个实现细节

### 订单 30 分钟未支付自动取消

下单事务提交后，往 `mall.order.delay.exchange` 发一条延迟消息，路由到 `mall.order.delay.queue`。这个队列挂了死信交换机 `mall.order.dlx.exchange`，消息过期后会转到 `mall.order.cancel.queue`，消费者再从取消队列里处理。

TTL 默认取订单设置里的 `normal_order_overtime`，初始值是 1800 秒，也可以理解成 30 分钟。消息发送失败只记日志，不影响下单本身，订单仍然可以手动取消。

消费端不直接信消息。先执行：

```sql
UPDATE oms_order SET status = 4 WHERE id = ? AND status = 0
```

只有这条更新成功，才回滚 SKU 库存、优惠券和积分。重复投递、已支付或已关闭的订单都会更新失败，然后直接忽略，所以同一条超时消息来多次也不会重复退款。

### 商品保存

创建商品一次要写 8 张表：`pms_product`、`pms_member_price`、`pms_product_ladder`、`pms_product_full_reduction`、`pms_sku_stock`、`pms_product_attribute_value`，再加专题和优选专区两张关联表。

更新商品用的是“先删子表、再全量重插”，写法简单，但子表的自增 id 会变。SKU 编码按 `yyyyMMdd + 商品 id + 3 位序号` 生成。

### 权限

后台菜单和接口资源是分开的。`ums_menu` 管左侧导航显不显示，`ums_resource` 存接口 URL 和请求方式，管接口能不能调。登录后按角色查出菜单和资源，前端控制导航，后端拦截器校验接口。

### 购物车价格

购物车表只存会员、商品、SKU 和数量，不存价格。每次查购物车时实时算会员价、满减和阶梯价，这样商品调价后不会出现购物车价格还是旧的情况。

## 项目结构

```text
mall/
├── mall-backend/       # Spring Boot 后端，同时提供后台和前台接口
│   └── src/main/java/com/macro/mall/
│       ├── controller/admin
│       ├── controller/portal
│       ├── service
│       ├── mapper
│       ├── mq             # 订单延迟消息和超时消费
│       └── auth           # 登录 Token 和权限拦截
├── mall-frontend/      # Vue 3 前端
│   └── src/views/{mall,member,admin}
├── sql/mall.sql        # 建表、初始化和演示数据
├── docs/screenshots/   # README 截图
└── docker-compose.yml  # MySQL、Redis、RabbitMQ
```

## 本地运行

需要 JDK 17、Node 18+ 和 Docker。如果本机已经装了 MySQL、Redis、RabbitMQ，也可以不用 Docker，直接改 `application.yml` 或传环境变量。

先起依赖：

```bash
docker compose up -d
```

默认会起 MySQL 3306（`root/root`，库名 `mall_vue`）、Redis 6379、RabbitMQ 5672 和管理台 15672（`guest/guest`）。MySQL 第一次启动时会自动执行 `sql/mall.sql`。

再起后端：

```powershell
cd mall-backend
.\mvnw.cmd spring-boot:run
```

macOS / Linux 用 `./mvnw spring-boot:run`。后端默认 8080。

最后起前端：

```bash
cd mall-frontend
npm install
npm run dev
```

打开 http://localhost:5173 是前台，http://localhost:5173/admin/login 是后台。

演示账号：管理员 `admin/123456`，运营 `operator/123456`，会员 `13800138000/123456`。

如果 MySQL 不在本机默认端口，改 `MYSQL_URL`、`MYSQL_USERNAME`、`MYSQL_PASSWORD` 就行。订单超时时间可以在 `application.yml` 的 `mall.order.timeout-minutes` 里改，也可以在后台“订单设置”里改，后者按秒存。

## 补充说明

支付宝是模拟的，订单状态流转和回调幂等逻辑保留了，接真实 SDK 时要替换支付网关和验签。

商品搜索用的是 MySQL `LIKE`，没有接 Elasticsearch。

RabbitMQ 没启动时，下单还能正常用，只是订单不会自动超时取消，需要手动取消。

演示账号和默认密码只适合本地跑，真要放到公网得先改掉。