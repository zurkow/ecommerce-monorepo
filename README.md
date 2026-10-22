# E-commerce Monorepo (POC)

A small but complete e-commerce system living in **one Git repository**:
four Spring Boot microservices, one shared Java library and a Vue.js admin panel.

The goal of this project is to **learn how a monorepo works** if you come from the
"one repository per microservice" (polyrepo) world.

```
                         ┌──────────────────────────┐
                         │   admin-panel (Vue 3)    │  :5173
                         └────────────┬─────────────┘
              /api/users   /api/carts │  /api/orders   /api/payments
          ┌────────────────┬──────────┴──────┬────────────────┐
          ▼                ▼                 ▼                ▼
   ┌─────────────┐  ┌─────────────┐  ┌──────────────┐  ┌───────────────┐
   │user-service │  │cart-service │  │order-service │  │payment-service│
   │    :8081    │  │    :8082    │  │    :8083     │  │     :8084     │
   └─────────────┘  └─────────────┘  └──────┬───────┘  └───────────────┘
          ▲                ▲   REST calls   │                ▲
          └────────────────┴────────────────┴────────────────┘
          each service has its OWN in-memory H2 database
```

---

## 1. Monorepo in 2 minutes

| | Polyrepo (what you do today) | Monorepo (this project) |
|---|---|---|
| Repos | 1 repo per service | 1 repo for everything |
| Shared code (DTOs, error handling) | separate `common` repo, publish jar to Nexus, bump version in N repos | `libs/common` folder, used directly, **no publishing** |
| Change an API used by 3 services | 3+ PRs in 3 repos, merge in the right order | **1 PR, 1 atomic commit** |
| Spring Boot upgrade | N PRs | change **1 line** in root `pom.xml` |
| Build | each repo builds itself | root build builds all; CI builds **only what changed** |
| Ownership | repo permissions | `CODEOWNERS` per folder |
| Deployment | 1 artifact per repo | **still 1 artifact per service** – nothing changes here! |

> **Important:** a monorepo is about *how the code is stored and built*,
> **not** about how it runs. These are still 4 independent microservices with
> 4 separate jars, 4 ports and 4 databases. A monorepo is **not** a monolith.

---

## 2. Repository layout

```
ecommerce-monorepo/
├── pom.xml                      ← ROOT: lists all Java modules + shared versions (parent POM)
├── package.json                 ← ROOT for JS: npm workspaces (apps/*)
├── libs/
│   └── common/                  ← shared library (plain jar, not runnable)
│       ├── contract/            ← DTOs exchanged between services (UserDto, CartDto, PaymentRequest...)
│       └── error/               ← one error format + GlobalExceptionHandler for every service
├── services/
│   ├── user-service/            ← Spring Boot app :8081
│   ├── cart-service/            ← Spring Boot app :8082
│   ├── order-service/           ← Spring Boot app :8083 (calls user, cart, payment)
│   └── payment-service/         ← Spring Boot app :8084 (fake gateway)
├── apps/
│   └── admin-panel/             ← Vue 3 + Vite admin panel :5173
├── scripts/                     ← start-all / stop-all / demo
└── .github/
    ├── workflows/ci.yml         ← builds ONLY affected projects
    └── CODEOWNERS               ← who reviews which folder
```

Common conventions you will see in real monorepos: `services/` (deployables),
`libs/` or `packages/` (shared code), `apps/` (frontends), `tools/` or `scripts/`.

---

## 3. How it is wired (the 3 files to understand)

### 3.1 Root `pom.xml` – aggregator **and** parent
```xml
<parent>spring-boot-starter-parent 3.5.6</parent>   <!-- ONE Spring Boot version for all -->
<packaging>pom</packaging>
<modules>
    <module>libs/common</module>
    <module>services/user-service</module>
    ...
</modules>
```
- **Aggregator** → `mvn verify` in the root builds every module, in dependency order
  (Maven calls this the *reactor*).
- **Parent** → every module inherits Java 21, Spring Boot version and plugin config.

### 3.2 A service `pom.xml`
```xml
<parent>
    <artifactId>ecommerce-monorepo</artifactId>
    <relativePath>../../pom.xml</relativePath>     <!-- points to the root -->
</parent>
<dependency>
    <groupId>com.ecommerce</groupId>
    <artifactId>common</artifactId>                <!-- no version: managed by root -->
</dependency>
```

### 3.3 `libs/common` – shared code, auto-configured
`CommonAutoConfiguration` is registered in
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`,
so **just adding the dependency** gives every service the global exception handler.
Try it: `GET /api/users/999` and `POST /api/orders/checkout` with an empty cart return
exactly the same error JSON shape although they are different services.

**What to share and what not:** `CartDto` is in `common` because order-service consumes it.
`OrderDto` is **not** – only order-service uses it. Share contracts, not domain logic;
otherwise `common` becomes a "distributed monolith" that couples everyone.

---

## 4. Running it

Requirements: **Java 21**, **Maven 3.9+**, **Node 20+**.

```bash
# build + test everything (11 tests)
mvn verify

# start all 4 services (builds once, runs the jars in the background, waits for health)
./scripts/start-all.sh

# walk through the purchase flow with curl
./scripts/demo.sh

# admin panel (in another terminal)
npm install
npm run admin:dev          # → http://localhost:5173

# stop the services
./scripts/stop-all.sh
```

Or run a single service from your IDE / terminal:
```bash
mvn -pl services/user-service -am install -DskipTests   # once: build it + common
mvn -pl services/user-service spring-boot:run
```

Every service exposes:
- REST API under `/api/...`
- health: `http://localhost:808X/actuator/health`
- H2 console: `http://localhost:808X/h2-console` (JDBC URL `jdbc:h2:mem:userdb`, `cartdb`, `orderdb`, `paymentdb`, user `sa`, empty password)

Data lives in memory → **restarting a service wipes its data**. user-service seeds 3 users.

---

## 5. Daily monorepo commands (Maven)

These flags are the "monorepo toolkit" for Maven:

| Command | Meaning |
|---|---|
| `mvn verify` | build & test **everything** |
| `mvn verify -pl services/order-service` | **p**roject **l**ist: only order-service |
| `mvn verify -pl services/order-service -am` | **a**lso **m**ake: + what it depends on (`libs/common`) |
| `mvn verify -pl libs/common -amd` | **a**lso **m**ake **d**ependents: common + **every service using it** – "what did I break?" |
| `mvn verify -pl services/user-service,services/cart-service` | several projects |
| `mvn -T 4 verify` | build modules in parallel (4 threads) |

npm workspaces do the same for JS: `npm run build -w admin-panel`.

---

## 6. API reference

| Service | Method & path | Description |
|---|---|---|
| user | `GET /api/users` · `GET /api/users/{id}` | list / get |
| user | `POST /api/users` · `PUT /api/users/{id}` · `DELETE /api/users/{id}` | create / update / delete |
| cart | `GET /api/carts` | all non-empty carts (admin) |
| cart | `GET /api/carts/{userId}` | cart of a user (empty if none) |
| cart | `POST /api/carts/{userId}/items` | add item `{sku,name,unitPrice,quantity}` |
| cart | `DELETE /api/carts/{userId}/items/{sku}` · `DELETE /api/carts/{userId}` | remove item / clear |
| order | `POST /api/orders/checkout` `{userId}` | **checkout** (see flow below) |
| order | `GET /api/orders` · `GET /api/orders/{id}` | list / get |
| order | `PATCH /api/orders/{id}/status` `{status}` | `PAID→SHIPPED→DELIVERED`, `PAID→CANCELLED` (refunds) |
| payment | `POST /api/payments` `{orderId,userId,amount}` | pay; **> 1000 is DECLINED** (demo rule) |
| payment | `GET /api/payments` · `POST /api/payments/{id}/refund` | list / refund |

### Checkout flow (order-service)
```
1. user-service    GET  /api/users/{id}        → 404 if user doesn't exist
2. cart-service    GET  /api/carts/{userId}    → 409 if cart is empty
3. save order as PENDING (snapshot of cart lines and prices)
4. payment-service POST /api/payments          → APPROVED / DECLINED
5. order → PAID or PAYMENT_FAILED
6. cart-service    DELETE /api/carts/{userId}  (only if PAID)
```

---

## 7. CI: build only what changed

`.github/workflows/ci.yml` shows the most important monorepo CI pattern:

1. `dorny/paths-filter` checks which folders the commit/PR touched.
2. Only affected services are built, each with `mvn -pl services/<name> -am verify`, in parallel.
3. A change in `libs/**` or the root `pom.xml` marks **all** services as affected.
4. The admin panel is built only if `apps/admin-panel/**` changed.

So a typo fix in `cart-service` runs only cart-service tests, exactly like in your polyrepo
world – but a change in a shared DTO automatically tests every consumer.

---

## 8. Exercises (to really learn it)

1. **Atomic cross-service change.** Add a `currency` field to `PaymentRequest`/`PaymentDto`
   in `libs/common`. Run `mvn verify -pl libs/common -amd` – see which services need updating,
   fix them, and commit everything in **one** commit. Compare with how many PRs it would take today.
2. **Upgrade everything at once.** Change the Spring Boot version in the root `pom.xml` and run `mvn verify`.
3. **New service.** Add `services/product-service` (port 8085): copy a service POM, add it to `<modules>`,
   add a path filter in `ci.yml`, a line in `CODEOWNERS`, a proxy in `apps/admin-panel/vite.config.js`.
   Then make cart-service validate SKUs against it.
4. **Find the bug-by-design.** Refund a payment from the *Payments* page – the order stays `PAID`.
   Services own their data, so cross-service consistency needs a design (call back, events/Kafka, saga…).
5. **Resilience.** Stop payment-service and check out a cart – what state is the order left in?
   (Hint: it stays `PENDING`, and the API returns 502.) How would you fix it?
6. **Containers.** Add a `Dockerfile` per service and a root `docker-compose.yml`
   (use env vars like `SERVICES_CART_URL=http://cart-service:8082` for order-service).

---

## 9. When you outgrow plain Maven

Maven multi-module is the simplest monorepo tool for Java and is enough for tens of modules.
Big monorepos add tools for caching and "affected" detection across languages:

- **Gradle** (composite builds, build cache) – common Java alternative
- **Nx** (has Java/Gradle and Vue support), **Bazel**, **Pants** – polyglot, remote caching
- **Maven build cache extension** – incremental builds for Maven

Further reading / similar examples:
- [freeCodeCamp – How to Build Multi-Module Projects in Spring Boot for Scalable Microservices](https://www.freecodecamp.org/news/how-to-build-multi-module-projects-in-spring-boot-for-scalable-microservices/)
- [Medium – Creating microservices via a multi-module project in Spring Boot](https://medium.com/@bedmuthaapoorv/creating-microservices-via-a-multi-module-project-in-spring-boot-0f7cfeb24c48)
- [codejava.net – How to Create Multi-Module Spring Boot Maven Project](https://codejava.net/frameworks/spring-boot/create-multi-module-maven-project)
- [msboffl/ekart](https://github.com/msboffl/ekart) – Spring Boot microservices e-commerce monorepo (React admin)
- [codecentric – Spring Boot + Vue.js in one Maven build](https://www.codecentric.de/en/knowledge-hub/blog/spring-boot-vuejs)
