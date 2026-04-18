# NanoURL

URL shortener built with Spring Boot 4, featuring a REST API and a Thymeleaf/HTMX web UI.

## Background

A mini weekend backend project meant for me to practice and consolidate my skills in Java, Spring and its various ecosystem.

## Prerequisites

- Java 25
- Maven (or use the included `./mvnw` wrapper)

## Stack

- **Java 25**, Spring Boot 4, Spring Data JPA, Hibernate Validator
- **Thymeleaf** + **HTMX** + **Hyperscript** for the frontend
- **H2** (dev) / **PostgreSQL via Supabase** (prod)
- **springdoc-openapi** — Swagger UI at `/swagger-ui.html` (dev only)
- Lombok, Maven

## Running locally

```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`. The H2 console is available at `/h2-console`.

## Running in production

Set the following environment variables in a `.env` file in project root directory (loaded automatically via `spring.config.import`)

```bash
DB_URL=jdbc:postgresql://<host>/<db>
DB_USERNAME=<user>
DB_PASSWORD=<password>
```

Then run with the `prod` profile:

```
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## Running with Docker

```bash
# Build
docker build -t nanourl .

# Run (dev — H2 in-memory)
docker run -p 8080:8080 nanourl

# Run (prod — PostgreSQL)
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://<host>/<db> \
  -e DB_USERNAME=<user> \
  -e DB_PASSWORD=<password> \
  nanourl
```

## Docs

- **Swagger UI** — available at `/swagger-ui.html` in dev. Disabled in production.
- **UML class diagram** — [`docs/uml.mmd`](docs/uml.mmd) (Mermaid format)

## API

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/urls` | Create a short URL |
| `GET` | `/api/urls/{code}` | Get short URL metadata |
| `GET` | `/{code}` | Redirect to the target URL (302) |

### Error responses

All error responses return **plain text** with an HTTP 400 or 404 status. Example:

```
url: must not be blank
```

```
Alias already taken
```

### Create a short URL

```
POST /api/urls
Content-Type: application/json

{
  "url": "https://example.com",
  "alias": "my-alias",
  "expiryDays": 30
}
```

| Field | Required | Constraints |
|-------|----------|-------------|
| `url` | Yes | HTTP/HTTPS, max 2048 chars. A missing scheme defaults to `https://`. |
| `alias` | No | 3–32 chars, `[A-Za-z0-9_]` only. Returns 400 immediately if already taken. |
| `expiryDays` | No | 1–365. Omit for a non-expiring link. |

**Response — 201 Created**

```json
{
  "code": "abc1234",
  "targetUrl": "https://example.com",
  "lastAccessedAt": null,
  "createdAt": "2026-04-18T10:00:00Z"
}
```

### Get short URL metadata

```
GET /api/urls/{code}
```

Returns 200 with the same body as above, or 404 if the code does not exist or has expired.

### Redirect

```
GET /{code}
```

Resolves the short code and issues a **302** redirect to the target URL. Also registers a hit (increments the hit counter and updates `lastAccessedAt`).

Returns 404 if the code does not exist or has expired.

## Tests

```bash
./mvnw test
```

## Future works

I plan to extend this in separate repos to dive into topics like:

1. Analytics tracking
2. Rate limiting
3. Caching
4. Load testing


## References

List of references used creating this project:

1. https://medium.com/devdomain/building-a-url-shortener-in-java-and-spring-boot-9a7ac3fd70ba