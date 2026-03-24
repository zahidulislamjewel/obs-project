# OBS Backend Agent Instructions

You are a senior backend engineer responsible for building the backend of an Online Book Store (OBS).

## Technology Stack

* Java 21 (Amazon Corretto 21.0.10)
* Spring Boot 3.4.3
* Spring Data JPA (Hibernate 6.6.8)
* PostgreSQL 16 (Docker container)
* Docker + Docker Compose v2
* JUnit 5 + Mockito
* Maven 3.8.7

> Note: Spring Boot 4 was specified in the original requirements but is not yet available on Maven Central. Spring Boot 3.4.3 is the latest stable release and was used instead.

## Architecture

Clean layered architecture with constructor injection throughout.

```
Controller → Service (interface + impl) → Repository → Entity
                                      ↘ DTO
```

Package root: `com.obs.backend`

```
src/main/java/com/obs/backend/
├── ObsBackendApplication.java
├── controller/
│   ├── BookController.java
│   └── GlobalExceptionHandler.java
├── service/
│   ├── BookService.java          # interface
│   └── impl/
│       └── BookServiceImpl.java
├── repository/
│   ├── BookRepository.java
│   ├── AuthorRepository.java
│   └── CategoryRepository.java
├── entity/
│   ├── Book.java
│   ├── Author.java
│   └── Category.java
└── dto/
    ├── BookRequest.java
    └── BookResponse.java
```

## Database

PostgreSQL container (`obsdb` / `obsuser` / `obspass`).

### Entities

**Author** — `id`, `name`, `bio`

**Category** — `id`, `name`, `description`

**Book** — `id`, `title`, `description`, `price` (BigDecimal), `isbn` (unique), `publishedDate` (LocalDate), `author` (ManyToOne), `category` (ManyToOne), `stock`

### JPA Configuration

- `ddl-auto=create` — Hibernate creates schema on startup
- `spring.jpa.defer-datasource-initialization=true` — ensures `data.sql` runs after schema creation
- `spring.sql.init.mode=always` — always execute `data.sql`

## API Endpoints

Base path: `/api/books`

| Method | Path | Description | Status |
|--------|------|-------------|--------|
| GET | `/api/books` | List all books (paginated) | 200 |
| GET | `/api/books/{id}` | Get book by ID | 200 / 404 |
| POST | `/api/books` | Create a new book | 201 / 400 |
| PUT | `/api/books/{id}` | Update a book | 200 / 404 |
| DELETE | `/api/books/{id}` | Delete a book | 204 / 404 |

Pagination defaults: `page=0`, `size=10`, `sort=id`. Use `?page=N&size=N` query params.

Error responses use RFC 9457 `ProblemDetail` format (handled by `GlobalExceptionHandler`).

### BookRequest fields (POST / PUT body)

```json
{
  "title": "string (required)",
  "description": "string",
  "price": "decimal > 0 (required)",
  "isbn": "string (required)",
  "publishedDate": "YYYY-MM-DD",
  "authorId": "long (required)",
  "categoryId": "long (required)",
  "stock": "int >= 0 (required)"
}
```

## Docker

### Port mapping

| Service | Container port | Host port |
|---------|---------------|-----------|
| backend | 8080 | 8080 |
| postgres | 5432 | 5433 |

> Host port for postgres is `5433` (not 5432) because port 5432 is occupied by a local PostgreSQL instance on the dev machine.

### Run

```bash
docker compose up        # start (uses cached image)
docker compose up --build  # rebuild backend image first
docker compose down      # stop and remove containers
```

### Dockerfile

Multi-stage build:
1. `maven:3.9.9-eclipse-temurin-21` — builds the fat JAR (skips tests)
2. `eclipse-temurin:21-jre` — runs the JAR on port 8080

## Seed Data (`src/main/resources/data.sql`)

11 books across 5 authors and 4 categories. INSERTs use subqueries to reference authors/categories by name (avoids sequence/ID conflicts):

```sql
INSERT INTO books (..., author_id, category_id)
SELECT ..., a.id, c.id FROM authors a, categories c
WHERE a.name = 'George Orwell' AND c.name = 'Fiction';
```

**Authors:** George Orwell, J.K. Rowling, F. Scott Fitzgerald, Harper Lee, J.R.R. Tolkien

**Categories:** Fiction, Fantasy, Classic Literature, Science Fiction

## Testing

### Run tests

```bash
mvn test
```

18 tests, all passing.

### Test configuration

Tests use H2 in-memory database (`src/test/resources/application.properties`) with `MODE=PostgreSQL`. `spring.sql.init.mode=never` prevents `data.sql` from running in tests.

### Test classes

**`BookServiceTest`** (10 tests, `@ExtendWith(MockitoExtension.class)`)
- Mocks `BookRepository`, `AuthorRepository`, `CategoryRepository`
- Covers: `getAllBooks` (pagination), `getBookById` (found/not found), `createBook` (success/author not found/category not found), `updateBook` (found/not found), `deleteBook` (found/not found)

**`BookControllerTest`** (8 tests, `@WebMvcTest + @MockitoBean`)
- Uses `MockMvc` to test HTTP layer
- Covers: GET all, GET by ID (found/not found), POST (valid/invalid), PUT, DELETE (found/not found)

## Git Workflow

Branch: `dev/backend/OBS-1`
Remote: `https://github.com/zahidulislamjewel/obs-backend.git`
PR: `https://github.com/zahidulislamjewel/obs-backend/pull/1`

Commit convention:
```
feat: <description>   # new functionality
test: <description>   # tests
chore: <description>  # config / tooling
fix: <description>    # bug fixes
```

## Docker Compose v2 Setup

Docker Compose v2 plugin is installed at `~/.docker/cli-plugins/docker-compose` (downloaded manually — not available via apt without sudo).

```bash
docker compose version  # Docker Compose version v2.33.1
```

## Known Issues / Notes

- `isbn` column has a unique constraint. Attempting to create a book with a duplicate ISBN will result in a 500 (DB constraint violation). A unique ISBN validation at the service layer can be added if needed.
- Authentication is not implemented yet.
