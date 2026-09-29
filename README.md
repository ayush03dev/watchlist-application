# Watchlist Service

Spring Boot service that stores per-user watch lists in PostgreSQL. Items are ordered by most recently added or bumped (`addedAt` descending). Capacity and pagination limits are configurable.

## Prerequisites

- Java (project uses Java 25 in `pom.xml`; adjust if needed)
- Maven (wrapper included: `./mvnw`)
- Docker Desktop (or Docker Engine) for PostgreSQL

## Setup

### 1. Start PostgreSQL

From the project root:

```bash
docker compose up -d
```

Wait until the container is healthy:

```bash
docker compose ps
```

Default connection (used by the `local` profile):

| Setting  | Value        |
|----------|--------------|
| Host     | `localhost`  |
| Port     | `5432`       |
| Database | `watchlist`  |
| User     | `postgres`   |
| Password | `postgres`   |

### 2. Run the application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

The API listens on **http://localhost:8080**.

Verify health (includes DB when `local` profile is active):

```bash
curl -s http://localhost:8080/actuator/health
```

### 3. Run tests

**Unit tests** (use Testcontainers for DB-backed tests; Docker must be running):

```bash
./mvnw test
```

**Integration tests** (use the same Postgres from `docker compose` on `localhost:5432`):

```bash
./mvnw test -Pintegration
```

## Configuration

Key settings in `src/main/resources/application.yml`:

| Property | Default | Description |
|----------|---------|-------------|
| `watchlist.max-size` | `500` | Max items per user |
| `watchlist.max-page-size` | `50` | Max `size` on list API |
| `watchlist.allowed-user-ids` | `1, 2, 3, 4` | Allowed `userId` values |

## API overview

Base path: **`/api/v1/watchlist/items`**

| Method | Description |
|--------|-------------|
| `PUT` | Add item, or **bump** `addedAt` if it already exists (moves to top) |
| `DELETE` | Remove item (idempotent: **204** even if missing) |
| `GET` | Paginated list for a user, newest first |

### Request / response notes

- **PUT** body: `{ "userId": <int>, "contentId": "<string>" }`
  - **201** — new item (`bumped: false`)
  - **200** — existing item updated (`bumped: true`)
  - **409** — watch list full (`code`: `WATCHLIST_FULL`, includes `maxSize`)
- **DELETE** body: same as PUT → **204** no body
- **GET** query: `userId` (required), `page` (default `0`), `size` (default `20`, capped by `max-page-size`)
- Errors: JSON with `code`, `message`, `timestamp`, `path`; `maxSize` on full list

---

## Manual validation (curl)

Set the base URL once:

```bash
export BASE=http://localhost:8080/api/v1/watchlist/items
```

Optional: clear test data for users 1 and 4:

```bash
docker exec watchlist-pg psql -U postgres -d watchlist \
  -c "DELETE FROM watchlist_item WHERE user_id IN (1, 4);"
```

### 1. Add new item (expect HTTP 201, `bumped: false`)

```bash
curl -i -X PUT "$BASE" -H 'Content-Type: application/json' \
  -d '{"userId":1,"contentId":"live-movie-1"}'
```

### 2. Bump same item (expect HTTP 200, `bumped: true`)

```bash
curl -i -X PUT "$BASE" -H 'Content-Type: application/json' \
  -d '{"userId":1,"contentId":"live-movie-1"}'
```

### 3. Add second item (expect HTTP 201)

```bash
curl -i -X PUT "$BASE" -H 'Content-Type: application/json' \
  -d '{"userId":1,"contentId":"live-movie-2"}'
```

### 4. Bump first item again (expect HTTP 200; list should show `live-movie-1` first)

```bash
curl -i -X PUT "$BASE" -H 'Content-Type: application/json' \
  -d '{"userId":1,"contentId":"live-movie-1"}'
```

### 5. List items (expect HTTP 200, ordered by `addedAt` desc)

```bash
curl -i "$BASE?userId=1&page=0&size=10"
```

### 6. Pagination (expect HTTP 200; `page=1`, `size=1` returns second item)

```bash
curl -i "$BASE?userId=1&page=1&size=1"
```

### 7. Remove item (expect HTTP 204)

```bash
curl -i -X DELETE "$BASE" -H 'Content-Type: application/json' \
  -d '{"userId":1,"contentId":"live-movie-2"}'
```

### 8. List after delete (expect only remaining items)

```bash
curl -i "$BASE?userId=1&page=0&size=10"
```

### 9. Delete missing item — idempotent (expect HTTP 204)

```bash
curl -i -X DELETE "$BASE" -H 'Content-Type: application/json' \
  -d '{"userId":1,"contentId":"no-such-item"}'
```

### 10. Invalid userId (expect HTTP 400, `VALIDATION_ERROR`)

```bash
curl -i -X PUT "$BASE" -H 'Content-Type: application/json' \
  -d '{"userId":99,"contentId":"x"}'
```

### 11. Blank contentId (expect HTTP 400)

```bash
curl -i -X PUT "$BASE" -H 'Content-Type: application/json' \
  -d '{"userId":1,"contentId":""}'
```

### 12. Invalid page (expect HTTP 400)

```bash
curl -i "$BASE?userId=1&page=-1&size=10"
```

### 13. Capacity — fill user 4 to max (500 items; takes ~1–2 minutes)

Use a dedicated user so you do not mix with other tests:

```bash
for i in $(seq 1 500); do
  curl -s -o /dev/null -X PUT "$BASE" -H 'Content-Type: application/json' \
    -d "{\"userId\":4,\"contentId\":\"cap-$i\"}"
done
```

Check count in Postgres:

```bash
docker exec watchlist-pg psql -U postgres -d watchlist \
  -c "SELECT count(*) FROM watchlist_item WHERE user_id=4;"
```

### 14. Add when full (expect HTTP 409, `WATCHLIST_FULL`)

```bash
curl -i -X PUT "$BASE" -H 'Content-Type: application/json' \
  -d '{"userId":4,"contentId":"cap-overflow"}'
```

### 15. Bump when full (expect HTTP 200; count stays at max)

```bash
curl -i -X PUT "$BASE" -H 'Content-Type: application/json' \
  -d '{"userId":4,"contentId":"cap-1"}'
```

---

## Project layout

| Package | Role |
|---------|------|
| `api` | REST controller, DTOs, validation |
| `domain` | `WatchlistService`, domain models |
| `persistence` | JPA entity and repository |
| `config` | Properties, request logging filter |
| `exception` | Global error handling |

## Stop services

```bash
# Stop app: Ctrl+C in the terminal running spring-boot:run

docker compose down
```

To remove the database volume as well:

```bash
docker compose down -v
```
