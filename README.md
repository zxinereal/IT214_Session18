# ShopMart Microservices - Saga Distributed Transaction & Spring Cloud Infrastructure

Dự án Microservice phục vụ bài kiểm tra thực hành Java Microservice (Session 14) hệ thống Thương mại Điện tử **ShopMart**.

---

## 🏛️ Kiến Trúc Hệ Thống

```
                                    +-----------------------+
                                    |  API Gateway (8080)   |
                                    +-----------+-----------+
                                                |
                      +-------------------------+-------------------------+
                      |                         |                         |
                      v                         v                         v
               +--------------+        +------------------+     +------------------+
               | order-service| Feign  |inventory-service |     | payment-service  |
               |   (8083)     +------->|   (8081/8084)    |     |     (8082)       |
               +------+-------+        +--------+---------+     +--------+---------+
                      |                         |                        |
                      +-------------------------+------------------------+
                                                |
                                                v
                                       +-----------------+
                                       |  Apache Kafka   | (Topics: order-events,
                                       +-----------------+  inventory-events, payment-events)
```

---

## 🚀 Danh Sách Microservice & Hạ Tầng

| Component | Port | Mô tả | Công nghệ chính |
| :--- | :--- | :--- | :--- |
| **Zookeeper & Kafka** | 2181 / 9092 | Message Broker phục vụ truyền sự kiện | Docker Compose, Confluent Kafka 7.5 |
| **Redis Server** | 6379 | Distributed Cache | Docker Compose, Redis Alpine 7.2 |
| **Config Server** | 8888 | Server cấu hình tập trung | Spring Cloud Config Server (Native Profile) |
| **Eureka Server** | 8761 | Service Discovery & Registry Dashboard | Spring Cloud Netflix Eureka Server |
| **API Gateway** | 8080 | Single Entry Point, Route requests | Spring Cloud Gateway, LoadBalancer |
| **Inventory Service** | 8081 / 8084 | Quản lý tồn kho & Cache-Aside | Spring Boot JPA, Spring Data Redis, Kafka Listener |
| **Payment Service** | 8082 | Xử lý thanh toán & Giả lập lỗi | Spring Boot JPA, Kafka Listener |
| **Order Service** | 8083 | Quản lý đơn hàng & Saga Orchestrator | FeignClient, Resilience4j Circuit Breaker, WebFlux |

---

## 📋 Chi Tiết Giải Quyết Các Câu Hỏi

### Câu 1: Hạ Tầng Microservice (Config Server, Eureka, Gateway)
- **Config Server (8888)**: Khai báo `@EnableConfigServer`, nạp file cấu hình từ `config-repo/`.
- **Eureka Server (8761)**: Dashboard theo dõi trực quan trạng thái và IP/Port của các dịch vụ.
- **API Gateway (8080)**: Routing động tới các service:
  - `/api/order/**` -> `order-service`
  - `/api/inventory/**` -> `inventory-service`
  - `/api/payment/**` -> `payment-service`

---

### Câu 2: Giao Tiếp Đồng Bộ & Circuit Breaker (Resilience4j)
- **OpenFeign Client**: `InventoryFeignClient` trong `order-service` trỏ tên service Eureka `inventory-service`.
- **Circuit Breaker**: Cấu hình `@CircuitBreaker(name = "inventoryService", fallbackMethod = "getProductStockFallback")`. Khi `inventory-service` bị ngưng, fallback được kích hoạt lập tức trả về đối tượng fallback safety thay vì ném ngoại lệ 500.
- **Load Balancing**: Hỗ trợ khởi chạy 2 instance `inventory-service` (ví dụ port 8081 và 8084).

---

### Câu 3: Giao Dịch Phân Tán (Saga Pattern) & Event-Driven (Kafka + WebFlux)
- **Chuỗi sự kiện (Happy Path)**:
  1. REST Client gửi POST `/api/order/create` -> `order-service` lưu Order ở trạng thái `PENDING` -> Phát sự kiện `ORDER_CREATED` qua Kafka topic `order-events`.
  2. `inventory-service` nhận sự kiện -> Khóa/Trừ tồn kho (`reserveStock`) -> Phát sự kiện `INVENTORY_RESERVED` qua Kafka topic `inventory-events`.
  3. `payment-service` nhận sự kiện -> Xử lý thanh toán -> Phát sự kiện `PAYMENT_PROCESSED` qua Kafka topic `payment-events`.
  4. `order-service` nhận `PAYMENT_PROCESSED` -> Cập nhật trạng thái đơn thành `APPROVED`.

- **Chuỗi bù hoàn tác (Saga Rollback Demonstration)**:
  - Cố tình truyền `paymentMethod = "FAIL"` hoặc bật `/api/payment/simulate-failure?fail=true`.
  - Khi thanh toán thất bại, `payment-service` phát sự kiện `PAYMENT_FAILED`.
  - `inventory-service` tiêu thụ `PAYMENT_FAILED` -> Thực hiện **Compensating Action** (`releaseStock`), hoàn trả tồn kho vừa khóa.
  - `order-service` tiêu thụ `PAYMENT_FAILED` -> Cập nhật trạng thái đơn thành `CANCELLED`.
  - Tất cả bước bù được ghi log chi tiết bằng **SLF4J**.

- **Reactive Async Consumer (WebFlux)**:
  - Endpoint `/api/order/events/stream` sử dụng Spring WebFlux Server-Sent Events (SSE) `Flux<OrderEvent>` để stream sự kiện bất đồng bộ theo real-time.

---

### Câu 4: Distributed Caching (Redis Cache-Aside)
- Tích hợp `spring-boot-starter-data-redis` trong `inventory-service`.
- Sử dụng `@Cacheable(value = "product_inventory", key = "#productId")`, `@CachePut`, và `@CacheEvict`.
- **Minh chứng Cache-Aside**:
  - Lần 1 gọi `GET /api/inventory/product/P1001`: Đọc từ DB (xuất hiện log `--> [DB QUERY]`).
  - Lần 2 gọi `GET /api/inventory/product/P1001`: Trả về trực tiếp từ Redis cache (không truy vấn DB).

---

## 🛠️ Hướng Dẫn Khởi Chạy

### 1. Khởi chạy hạ tầng Docker (Kafka, Zookeeper, Redis)
```bash
docker-compose up -d
```

### 2. Khởi chạy các Microservices (theo thứ tự khuyến nghị)
1. `config-server` (Port 8888)
2. `eureka-server` (Port 8761)
3. `inventory-service` (Port 8081)
4. `payment-service` (Port 8082)
5. `order-service` (Port 8083)
6. `api-gateway` (Port 8080)

Khởi chạy từng service qua wrapper:
```cmd
.\mvnw.cmd spring-boot:run -pl <module-name>
```

---

## 🧪 Kiểm Thử Tự Động (Unit & Saga Rollback Test)

Chạy bộ test kiểm thử tích hợp trong `order-service`:
```cmd
.\mvnw.cmd test -pl order-service
```
Bộ test bao gồm:
- `testCreateOrderInitiatesSaga`: Kiểm tra tạo đơn ở trạng thái PENDING và bắn Kafka event.
- `testSagaRollbackOnPaymentFailure`: Kiểm tra bù hoàn tác chuyển Order về CANCELLED khi thanh toán hỏng.
- `testSagaSuccessOnPaymentProcessed`: Kiểm tra chuỗi Saga thành công chuyển Order về APPROVED.
- `testInventoryCircuitBreakerFallback`: Kiểm tra Resilience4j Fallback khi Inventory Service ngưng.
