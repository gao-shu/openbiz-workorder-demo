# openbiz-workorder-demo

通过 Maven 依赖独立消费 OpenBiz 工单能力的示例工程。

```text
com.openbiz:openbiz-service:3.9.2  （本地 Maven 仓库）
        ↓
本项目（Spring Boot，端口 18081）
        ↓
真实 HTTP + 真实 MySQL
```

母仓库：[gao-shu/openbiz](https://github.com/gao-shu/openbiz)

---

## 本示例验证什么

- **独立** Git 仓库 / 独立进程，可通过 Maven 制品依赖 `openbiz-service`
- Controller 只调用 `WorkOrderService`（不复制状态机 / 租户守卫 / Mapper）
- 工单 API 对接 **真实 MySQL**，具备租户隔离与创建幂等

## 本示例不证明什么

| 不要这样宣称 | 原因 |
| --- | --- |
| 已发布到 Maven Central | 制品安装在**本地** `~/.m2` |
| 已产品化为 SDK | 仅为示例消费者，不是产品化 SDK |
| 生产可用 | 本地 Demo 配置与示例密码 |
| 纯 Spring Boot、无 RuoYi | 仍使用 **RuoYi 薄壳**（`ruoyi-framework`）提供登录 / 安全 / Redis |
| 真实 IoT / MQTT | 本示例不覆盖 |

---

## 前置条件

- **JDK 21**
- **Maven** 3.8+
- **MySQL**（默认库名 `ry-vue`）
- **Redis**（RuoYi 薄壳依赖）
- 可访问母仓库以便本地安装制品

**耗时说明：**约 5–10 分钟（假设上述工具已安装，且 MySQL / Redis 已运行）。

---

## 1. 安装 OpenBiz Service 制品（本地）

在母仓库 `open-biz-platform` 中执行：

```bash
mvn -pl openbiz-service,ruoyi-framework -am install -DskipTests
```

会将 `com.openbiz:openbiz-service:3.9.2` 与 `com.ruoyi:ruoyi-framework:3.9.2`（及必要传递依赖）安装到**本地** Maven 仓库。  
本示例需要 `ruoyi-framework` 作为 RuoYi 薄壳（登录 / 安全 / Redis）；只安装 `openbiz-service` 不够。  
**这不是 Maven Central。**

---

## 2. 配置数据库

默认数据源（`src/main/resources/application-druid.yml`）：

- URL：`jdbc:mysql://localhost:3306/ry-vue?...`
- 用户名 / 密码：**本地 Demo 值**，请按本机环境修改

母仓库 `sql/` 目录最低导入顺序大致为：

1. RuoYi 基础（`ry_20260417.sql`）
2. OpenBiz SaaS（`openbiz_saas_1_2.sql`）— 租户 / 成员
3. OpenBiz Service（`openbiz_service_1.sql`）— 工单表

跑通本工单 Demo **不需要**导入全部行业 IoT / Shop 脚本。

同时确保 Redis 已启动（默认见 `application.yml` 中的 `localhost:6379`）。

---

## 3. 启动

```bash
mvn spring-boot:run
```

期望日志包含：

```text
Started WorkOrderDemoApplication
```

端口：**18081**（不是 18080）。

---

## 4. 登录

```http
POST http://127.0.0.1:18081/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123",
  "code": "",
  "uuid": ""
}
```

将返回的 `token` 作为 `Authorization: Bearer <token>` 使用。

（种子用户取决于你导入的 RuoYi / OpenBiz SQL。本地配置中验证码可能已关闭。）

---

## 5. 首次调用工单接口

创建：

```http
POST http://127.0.0.1:18081/api/work-orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "First WO",
  "content": "from independent consumer",
  "contactName": "Ada",
  "contactPhone": "13800000000",
  "idempotentKey": "demo-first-001"
}
```

查询：

```http
GET http://127.0.0.1:18081/api/work-orders/{id}
Authorization: Bearer <token>
```

---

## API 一览（本 Demo）

| 方法 | 路径 | Service 调用 |
| --- | --- | --- |
| GET | `/api/work-orders` | `list()` |
| GET | `/api/work-orders/{id}` | `get(id)` |
| POST | `/api/work-orders` | `create(...)` |
| POST | `/api/work-orders/{id}/assign` | `assign(id, assigneeUserId)` |
| POST | `/api/work-orders/{id}/accept` | `accept(id)` |
| POST | `/api/work-orders/{id}/complete` | `complete(id, note)` |
| POST | `/api/work-orders/{id}/cancel` | `cancel(id)` |

生命周期（由母仓库 `WorkOrderTransitions` 提供，本仓库不重写）：

```text
CREATED → ASSIGNED → ACCEPTED → COMPLETED
CREATED | ASSIGNED → CANCELLED
COMPLETED → CANCELLED  → 拒绝（ILLEGAL_TRANSITION）
```

---

## 证据级别

**L3** — 独立进程 HTTP + 真实 MySQL（OpenBiz Phase 3 已验证）。

---

## 相关链接

- 母仓库文档：[快速开始](https://github.com/gao-shu/openbiz/blob/master/docs/getting-started.md)
- 证据边界：[docs/evidence.md](https://github.com/gao-shu/openbiz/blob/master/docs/evidence.md)
