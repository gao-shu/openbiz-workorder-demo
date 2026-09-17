# openbiz-workorder-demo

**Independent consumer** of OpenBiz WorkOrder capabilities via Maven.

```text
com.openbiz:openbiz-service:3.9.2  (local Maven repository)
        ↓
this project (Spring Boot, port 18081)
        ↓
Real HTTP + Real MySQL
```

Mother project: [gao-shu/openbiz](https://github.com/gao-shu/openbiz)

---

## What this proves

- A **separate** Git repo / process can depend on `openbiz-service` as a **Maven artifact**
- Controller calls `WorkOrderService` only (no copied state machine / tenant / mapper)
- WorkOrder APIs work against **real MySQL** with tenant isolation and create idempotency

## What this does **not** prove

| Do not claim | Why |
| --- | --- |
| Maven Central publishing | Artifact is installed to **local** `~/.m2` |
| SDK productization | One example consumer, not a productized SDK |
| Production readiness | Local demo config, sample passwords |
| Pure Spring Boot without RuoYi | Still uses **RuoYi thin shell** (`ruoyi-framework`) for login/security/Redis |
| Real IoT / MQTT | Out of scope for this example |

---

## Prerequisites

- **JDK 21**
- **Maven** 3.8+
- **MySQL** (default DB name `ry-vue`)
- **Redis** (required by the RuoYi thin shell)
- Mother repo available to install artifacts locally

**Time note:** ��5–10 minutes�� assumes these tools are already installed and MySQL/Redis are running.

---

## 1. Install OpenBiz Service artifact (local)

From the mother repository `open-biz-platform`:

```bash
mvn -pl openbiz-service,ruoyi-framework -am install -DskipTests
```

This installs `com.openbiz:openbiz-service:3.9.2` and `com.ruoyi:ruoyi-framework:3.9.2` (plus required reactor artifacts) into your **local** Maven repository.  
The Example needs `ruoyi-framework` for the RuoYi thin shell (login/security/Redis); installing only `openbiz-service` is not enough.  
**This is not Maven Central.**

---

## 2. Configure database

Default datasource (`src/main/resources/application-druid.yml`):

- URL: `jdbc:mysql://localhost:3306/ry-vue?...`
- User / password: **local demo values** �� change them for your machine

Minimum SQL from the mother `sql/` folder (import order roughly):

1. RuoYi base (`ry_20260417.sql`)
2. OpenBiz SaaS (`openbiz_saas_1_2.sql`) �� tenants / members
3. OpenBiz Service (`openbiz_service_1.sql`) �� work order tables

You do **not** need every industry IoT/Shop SQL script just to run this WorkOrder demo.

Also ensure Redis is up (`localhost:6379` by default in `application.yml`).

---

## 3. Run

```bash
mvn spring-boot:run
```

Expect:

```text
Started WorkOrderDemoApplication
```

Port: **18081** (not 18080).

---

## 4. Login

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

Use the returned `token` as `Authorization: Bearer <token>`.

(Seed users depend on your imported RuoYi/OpenBiz SQL. Captcha may be disabled in local config.)

---

## 5. First WorkOrder calls

Create:

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

Get:

```http
GET http://127.0.0.1:18081/api/work-orders/{id}
Authorization: Bearer <token>
```

---

## API surface (this demo)

| Method | Path | Service call |
| --- | --- | --- |
| GET | `/api/work-orders` | `list()` |
| GET | `/api/work-orders/{id}` | `get(id)` |
| POST | `/api/work-orders` | `create(...)` |
| POST | `/api/work-orders/{id}/assign` | `assign(id, assigneeUserId)` |
| POST | `/api/work-orders/{id}/accept` | `accept(id)` |
| POST | `/api/work-orders/{id}/complete` | `complete(id, note)` |
| POST | `/api/work-orders/{id}/cancel` | `cancel(id)` |

Lifecycle (provided by mother `WorkOrderTransitions`, not reimplemented here):

```text
CREATED → ASSIGNED → ACCEPTED → COMPLETED
CREATED | ASSIGNED → CANCELLED
COMPLETED → CANCELLED  → rejected (ILLEGAL_TRANSITION)
```

---

## Evidence level

**L3** �� Independent process HTTP + real MySQL (validated in OpenBiz Phase 3).

---

## Links

- Mother docs: [Getting Started](https://github.com/gao-shu/openbiz/blob/master/docs/getting-started.md)
- Evidence: [docs/evidence.md](https://github.com/gao-shu/openbiz/blob/master/docs/evidence.md)
