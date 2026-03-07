# OBS Backend — API Contract

**Base URL:** `http://localhost:8080`
**Content-Type:** `application/json`
**Version:** 1.0.0

---

## Common Schemas

### BookResponse

Returned by all read and write operations.

```json
{
  "id":           1,
  "title":        "1984",
  "description":  "A dystopian novel...",
  "price":        12.99,
  "isbn":         "978-0451524935",
  "publishedDate":"1949-06-08",
  "authorId":     1,
  "authorName":   "George Orwell",
  "categoryId":   4,
  "categoryName": "Science Fiction",
  "stock":        150
}
```

| Field | Type | Nullable | Description |
|-------|------|----------|-------------|
| `id` | `long` | no | Auto-generated primary key |
| `title` | `string` | no | Book title |
| `description` | `string` | yes | Book description |
| `price` | `decimal` | no | Price (2 decimal places) |
| `isbn` | `string` | no | International Standard Book Number (unique) |
| `publishedDate` | `string (ISO 8601)` | yes | Format: `YYYY-MM-DD` |
| `authorId` | `long` | no | ID of the associated author |
| `authorName` | `string` | no | Name of the associated author |
| `categoryId` | `long` | no | ID of the associated category |
| `categoryName` | `string` | no | Name of the associated category |
| `stock` | `int` | no | Available stock quantity |

---

### BookRequest

Request body for `POST /api/books` and `PUT /api/books/{id}`.

```json
{
  "title":        "1984",
  "description":  "A dystopian novel...",
  "price":        12.99,
  "isbn":         "978-0451524935",
  "publishedDate":"1949-06-08",
  "authorId":     1,
  "categoryId":   4,
  "stock":        150
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `title` | `string` | yes | Must not be blank |
| `description` | `string` | no | — |
| `price` | `decimal` | yes | Must be > 0 |
| `isbn` | `string` | yes | Must not be blank; must be unique across all books |
| `publishedDate` | `string (ISO 8601)` | no | Format: `YYYY-MM-DD` |
| `authorId` | `long` | yes | Must reference an existing author |
| `categoryId` | `long` | yes | Must reference an existing category |
| `stock` | `int` | yes | Must be >= 0 |

---

### ProblemDetail (Error Response)

All error responses follow [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) `ProblemDetail` format.

```json
{
  "type":     "about:blank",
  "title":    "Not Found",
  "status":   404,
  "detail":   "Book not found with id: 99",
  "instance": "/api/books/99"
}
```

---

### Page\<BookResponse\>

Paginated list wrapper returned by `GET /api/books`.

```json
{
  "content": [ /* array of BookResponse */ ],
  "pageable": {
    "pageNumber": 0,
    "pageSize":   10,
    "sort": { "sorted": true, "unsorted": false, "empty": false }
  },
  "totalElements": 11,
  "totalPages":    2,
  "first":         true,
  "last":          false,
  "size":          10,
  "number":        0,
  "numberOfElements": 10,
  "empty":         false
}
```

---

## Endpoints

---

### GET /api/books

Returns a paginated list of all books.

**Query Parameters**

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | `int` | `0` | Zero-based page number |
| `size` | `int` | `10` | Number of items per page |
| `sort` | `string` | `id,asc` | Sort field and direction (e.g. `title,desc`) |

**Example Request**

```http
GET /api/books?page=0&size=10&sort=title,asc
```

**Response — 200 OK**

```json
{
  "content": [
    {
      "id": 2,
      "title": "Animal Farm",
      "description": "An allegorical novella...",
      "price": 9.99,
      "isbn": "978-0451526342",
      "publishedDate": "1945-08-17",
      "authorId": 1,
      "authorName": "George Orwell",
      "categoryId": 1,
      "categoryName": "Fiction",
      "stock": 120
    }
  ],
  "totalElements": 11,
  "totalPages": 2,
  "first": true,
  "last": false,
  "size": 10,
  "number": 0
}
```

---

### GET /api/books/{id}

Returns a single book by its ID.

**Path Parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | `long` | Book ID |

**Example Request**

```http
GET /api/books/1
```

**Response — 200 OK**

```json
{
  "id": 1,
  "title": "1984",
  "description": "A dystopian social science fiction novel...",
  "price": 12.99,
  "isbn": "978-0451524935",
  "publishedDate": "1949-06-08",
  "authorId": 1,
  "authorName": "George Orwell",
  "categoryId": 4,
  "categoryName": "Science Fiction",
  "stock": 150
}
```

**Response — 404 Not Found**

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Book not found with id: 99",
  "instance": "/api/books/99"
}
```

---

### POST /api/books

Creates a new book.

**Request Body** — `BookRequest`

```json
{
  "title": "Brave New World",
  "description": "A dystopian novel by Aldous Huxley.",
  "price": 11.49,
  "isbn": "978-0060850524",
  "publishedDate": "1932-08-01",
  "authorId": 1,
  "categoryId": 4,
  "stock": 75
}
```

**Response — 201 Created**

```json
{
  "id": 12,
  "title": "Brave New World",
  "description": "A dystopian novel by Aldous Huxley.",
  "price": 11.49,
  "isbn": "978-0060850524",
  "publishedDate": "1932-08-01",
  "authorId": 1,
  "authorName": "George Orwell",
  "categoryId": 4,
  "categoryName": "Science Fiction",
  "stock": 75
}
```

**Response — 400 Bad Request** (validation failure)

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "title: Title is required, price: Price must be greater than 0, isbn: ISBN is required",
  "instance": "/api/books"
}
```

**Response — 404 Not Found** (author or category not found)

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Author not found with id: 99",
  "instance": "/api/books"
}
```

---

### PUT /api/books/{id}

Fully replaces an existing book. All `BookRequest` fields apply.

**Path Parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | `long` | ID of the book to update |

**Request Body** — `BookRequest`

```json
{
  "title": "1984 (Revised Edition)",
  "description": "Updated description.",
  "price": 13.99,
  "isbn": "978-0451524935",
  "publishedDate": "1949-06-08",
  "authorId": 1,
  "categoryId": 4,
  "stock": 200
}
```

**Response — 200 OK**

```json
{
  "id": 1,
  "title": "1984 (Revised Edition)",
  "description": "Updated description.",
  "price": 13.99,
  "isbn": "978-0451524935",
  "publishedDate": "1949-06-08",
  "authorId": 1,
  "authorName": "George Orwell",
  "categoryId": 4,
  "categoryName": "Science Fiction",
  "stock": 200
}
```

**Response — 400 Bad Request** — same as POST

**Response — 404 Not Found** — same as POST, or book ID not found

---

### DELETE /api/books/{id}

Deletes a book by its ID.

**Path Parameters**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | `long` | ID of the book to delete |

**Example Request**

```http
DELETE /api/books/1
```

**Response — 204 No Content**

Empty body.

**Response — 404 Not Found**

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Book not found with id: 1",
  "instance": "/api/books/1"
}
```

---

## HTTP Status Code Summary

| Code | Meaning | When |
|------|---------|------|
| 200 | OK | Successful GET or PUT |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Validation errors in request body |
| 404 | Not Found | Book, author, or category ID does not exist |
| 500 | Internal Server Error | Unhandled exception (e.g. duplicate ISBN constraint violation) |

---

## cURL Examples

```bash
# List all books (page 1)
curl http://localhost:8080/api/books

# List page 2 with 5 items per page
curl "http://localhost:8080/api/books?page=1&size=5"

# Get a single book
curl http://localhost:8080/api/books/1

# Create a book
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Brave New World",
    "description": "A dystopian novel by Aldous Huxley.",
    "price": 11.49,
    "isbn": "978-0060850524",
    "publishedDate": "1932-08-01",
    "authorId": 1,
    "categoryId": 4,
    "stock": 75
  }'

# Update a book
curl -X PUT http://localhost:8080/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "1984 (Revised Edition)",
    "description": "Updated description.",
    "price": 13.99,
    "isbn": "978-0451524935",
    "publishedDate": "1949-06-08",
    "authorId": 1,
    "categoryId": 4,
    "stock": 200
  }'

# Delete a book
curl -X DELETE http://localhost:8080/api/books/1
```
