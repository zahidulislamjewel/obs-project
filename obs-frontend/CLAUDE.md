# OBS Frontend Agent Instructions

You are a senior frontend engineer responsible for building the UI of an Online Book Store.

## Technology

Angular 17 (standalone components)
TypeScript
Node.js 18
Docker

## Project Structure

```
obs-frontend/               ← git root
├── docker-compose.yml
└── obs-frontend/           ← Angular project root
    ├── Dockerfile
    ├── .dockerignore
    ├── package.json
    └── src/
        └── app/
            ├── models/book.ts
            ├── services/book.service.ts
            ├── components/
            │   ├── book-list/
            │   ├── book-detail/
            │   └── book-form/
            ├── app.routes.ts
            ├── app.component.ts
            └── app.config.ts
```

## Pages

Home page (Book List) — `/books`

Book Detail page — `/books/:id`

Add Book page — `/books/new`

Edit Book page — `/books/edit/:id`

## Book Model

```typescript
export interface Book {
  id: number;
  title: string;
  description: string;
  price: number;
  author: string;
  category: string;
  publishedDate: string;
  stock: number;
}
```

## Data Source

Mock API via `book.service.ts` using RxJS `of()`.

10 books pre-loaded. Full CRUD: getBooks, getBook, addBook, updateBook, deleteBook.

## UI Requirements

Components:

- `book-list` — grid of all books with View / Edit / Delete actions
- `book-detail` — full book info with edit/delete
- `book-form` — shared form for Add and Edit (detects mode via route param)

Routes:

```
/              → redirects to /books
/books
/books/new
/books/edit/:id
/books/:id
```

## Docker

**All dev tools are containerized. No local installation of Node, npm, TypeScript, or Angular CLI is required.**

### Dockerfile (`obs-frontend/Dockerfile`)

```dockerfile
FROM node:18-alpine

RUN npm install -g @angular/cli@17 typescript

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .

EXPOSE 4200
CMD ["ng", "serve", "--host", "0.0.0.0", "--poll", "2000"]
```

### docker-compose.yml (root)

```yaml
services:
  obs-frontend:
    build:
      context: ./obs-frontend
      dockerfile: Dockerfile
    ports:
      - "4200:4200"
    volumes:
      - ./obs-frontend:/app
      - /app/node_modules
    environment:
      - CHOKIDAR_USEPOLLING=true
```

### .dockerignore (`obs-frontend/.dockerignore`)

```
node_modules
dist
.angular
.vscode
*.md
```

## Git Workflow

Branch: `dev/frontend/OBS-2`

Remote: `https://github.com/zahidulislamjewel/obs-frontend.git`

PR: `https://github.com/zahidulislamjewel/obs-frontend/pull/1`

Commit style:

```
feat: create angular project
feat: add book model
feat: implement book service
feat: add book list component
feat: containerize node, npm, angular cli and typescript
```

## Running the Application

```
docker compose up
```

UI available at: `http://localhost:4200`

No local Node, npm, or Angular CLI installation required.

## Status

- [x] Angular 17 project created
- [x] Book model implemented
- [x] BookService with 10 mock books and CRUD
- [x] book-list, book-detail, book-form components
- [x] Routing configured
- [x] Dockerfile with globally installed Angular CLI + TypeScript
- [x] docker-compose.yml at project root
- [x] Branch pushed and PR opened
- [ ] Backend integration (future)

## Notes

- Angular standalone components (no NgModule)
- `FormsModule` used for template-driven forms in `book-form`
- `CommonModule` and `RouterLink` imported per component
- Global styles in `src/styles.css`
- `docker compose up --build` needed after Dockerfile changes
