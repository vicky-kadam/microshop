# Microshop (practice project, sirf code)

Docker, K8s aur CI/CD tumhe khud banana hai. Is project mein sirf application ka code hai.

```
frontend (index.html) -> api-gateway:8080 -> user-service:8081
                                          -> product-service:8082
                                          -> order-service:8083  (order banate waqt user aur product service ko call karta hai)
                                          -> har service MySQL se judti hai (user_db, product_db, order_db)
```

Java 21, Spring Boot 3.3.5, Maven.

## Environment variables (sab optional, defaults local ke liye hain)

| Service | Variable | Default | Matlab |
|---|---|---|---|
| user / product / order | `SERVER_PORT` | 8081 / 8082 / 8083 | service ka port |
| user / product / order | `DB_URL` | `jdbc:mysql://localhost:3306/<db>?createDatabaseIfNotExist=true...` | MySQL ka address |
| user / product / order | `DB_USERNAME` | `root` | DB user |
| user / product / order | `DB_PASSWORD` | (khaali) | DB password |
| order-service | `USER_SERVICE_URL` | `http://localhost:8081` | user-service ka address |
| order-service | `PRODUCT_SERVICE_URL` | `http://localhost:8082` | product-service ka address |
| api-gateway | `SERVER_PORT` | 8080 | gateway ka port |
| api-gateway | `USER_SERVICE_URL` | `http://localhost:8081` | |
| api-gateway | `PRODUCT_SERVICE_URL` | `http://localhost:8082` | |
| api-gateway | `ORDER_SERVICE_URL` | `http://localhost:8083` | |
| api-gateway | `CORS_ORIGINS` | `*` | frontend ko allow karne ke liye |

## API
- `GET/POST /api/users`, `GET /api/users/{id}`
- `GET/POST /api/products`, `GET /api/products/{id}`
- `GET/POST /api/orders`, `GET /api/orders/{id}`  (POST body: `{"userId":1,"productId":1,"quantity":2}`)

Sab Gateway (8080) se hote hain. Health check: `/actuator/health` (liveness/readiness: `/actuator/health/liveness`, `/actuator/health/readiness`).

## Local chalane ke liye
MySQL chalu ho, phir har service folder mein (alag terminal):
```
cd user-service && mvn spring-boot:run
```
`DB_PASSWORD` set karna na bhoolna. Tests: `mvn verify`.
Frontend: `frontend/index.html` browser mein kholo aur URL ke aakhir mein `?api=http://localhost:8080/api/` jodo.

## Tumhare jimme (DevOps)
Dockerfile har service ka, frontend ko nginx se serve karna (aur `/api/` ko gateway par proxy karna), docker-compose ya K8s manifests, secrets, CI/CD pipeline, Docker Hub push, minikube deploy.
