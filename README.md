# Online Book Store (OBS)

A full-stack Online Book Store built with Spring Boot and Angular, fully containerised with Docker Compose.

---

## Architecture

```
obs-project/
├── .claude/                    # Claude Code configuration
│   ├── agents/                 # Custom sub-agent definitions
│   ├── skills/                 # Custom skill definitions
│   └── mcp.json                # MCP server configuration
├── obs-backend/                # Spring Boot REST API
├── obs-frontend/               # Angular 17 SPA
├── infrastructure/             # Docker Compose & deployment
│   └── docker-compose.yml
└── docs/
    └── plans/                  # Implementation plans
```

### Tech Stack

| Layer      | Technology                              |
|------------|-----------------------------------------|
| Backend    | Java 21, Spring Boot 3.4.3, Spring Data JPA |
| Database   | PostgreSQL 16                           |
| Frontend   | Angular 17 (standalone), TypeScript     |
| Container  | Docker, Docker Compose v2               |

---

## Getting Started

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/) with Compose v2 (`docker compose version`)
- No local Java, Node, or npm installation required — everything runs in containers

### Run the Full Stack

```bash
docker compose -f infrastructure/docker-compose.yml up --build
```

| Service   | URL                              |
|-----------|----------------------------------|
| Frontend  | http://localhost:4200            |
| Backend   | http://localhost:8080/api/books  |
| Postgres  | localhost:5433 (host port)       |

Stop all services:

```bash
docker compose -f infrastructure/docker-compose.yml down
```

---

## Backend

Spring Boot REST API — source in `obs-backend/`.

### API Endpoints

| Method | Path                  | Description         | Status |
|--------|-----------------------|---------------------|--------|
| GET    | `/api/books`          | List all books (paginated) | 200 |
| GET    | `/api/books/{id}`     | Get book by ID      | 200 / 404 |
| POST   | `/api/books`          | Create a book       | 201 / 400 |
| PUT    | `/api/books/{id}`     | Update a book       | 200 / 404 |
| DELETE | `/api/books/{id}`     | Delete a book       | 204 / 404 |
| GET    | `/api/authors`        | List all authors    | 200 |
| GET    | `/api/categories`     | List all categories | 200 |

Pagination: `GET /api/books?page=0&size=10`

Error responses use [RFC 9457 Problem Detail](https://www.rfc-editor.org/rfc/rfc9457) format.

### BookRequest body (POST / PUT)

```json
{
  "title": "string",
  "description": "string",
  "price": 19.99,
  "isbn": "978-0-000-00000-0",
  "publishedDate": "2024-01-01",
  "authorId": 1,
  "categoryId": 2,
  "stock": 10
}
```

### Run Backend Tests

```bash
cd obs-backend
mvn test
```

18 unit and integration tests using JUnit 5, Mockito, and H2 in-memory database.

### Seed Data

On first startup, Hibernate creates the schema and `data.sql` seeds:
- 5 authors (George Orwell, J.K. Rowling, F. Scott Fitzgerald, Harper Lee, J.R.R. Tolkien)
- 4 categories (Fiction, Fantasy, Classic Literature, Science Fiction)
- 11 books

---

## Frontend

Angular 17 SPA — source in `obs-frontend/obs-frontend/`.

### Pages

| Route             | Description         |
|-------------------|---------------------|
| `/books`          | Book list with category filter |
| `/books/:id`      | Book detail         |
| `/books/new`      | Add a new book      |
| `/books/edit/:id` | Edit an existing book |

### Environment Configuration

| File                          | Used when        | Backend URL                   |
|-------------------------------|------------------|-------------------------------|
| `src/environments/environment.ts`      | Dev (`ng serve`) | `http://localhost:8080/api`   |
| `src/environments/environment.prod.ts` | Docker build     | `http://backend:8080/api`     |

---

## Development

### Backend (local, requires PostgreSQL)

```bash
cd obs-backend
mvn spring-boot:run
```

### Frontend (local, requires Node 18+)

```bash
cd obs-frontend/obs-frontend
npm install
ng serve
```

Frontend available at `http://localhost:4200`. Ensure the backend is running on port 8080 with CORS enabled for `http://localhost:4200`.

---

## Database

| Parameter | Value    |
|-----------|----------|
| Database  | `obsdb`  |
| User      | `obsuser`|
| Password  | `obspass`|
| Host port | `5433`   |

> Host port is `5433` (not `5432`) to avoid conflicts with a locally installed PostgreSQL instance.

---

## Project Conventions

- **Branch strategy:** feature branches (`dev/backend/*`, `dev/frontend/*`), integration branch (`integration/OBS-INT-1`)
- **Commit style:** `feat:`, `fix:`, `chore:`, `refactor:`, `test:`
- **Backend architecture:** `Controller → Service (interface + impl) → Repository → Entity`
- **Frontend:** Angular standalone components, no NgModule
