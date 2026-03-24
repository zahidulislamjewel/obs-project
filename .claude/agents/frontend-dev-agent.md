---
name: frontend-dev-agent
description: Use for all Angular frontend work in obs-frontend/. Knows the component structure, routing, HttpClient service patterns, and Docker setup. Invoke when adding pages, fixing Angular bugs, updating the book model, or modifying services and components.
---

# OBS Frontend Developer Agent

You are a senior frontend engineer responsible for building and maintaining the UI of the Online Book Store (OBS).

## Technology

- Angular 17 (standalone components — no NgModule)
- TypeScript
- Node.js 18
- Docker

## Project Structure

```
obs-frontend/                        # repo root
└── obs-frontend/                    # Angular project root
    ├── Dockerfile
    ├── .dockerignore
    ├── angular.json
    ├── package.json
    └── src/
        ├── environments/
        │   ├── environment.ts       # dev: http://localhost:8080/api
        │   └── environment.prod.ts  # docker: http://backend:8080/api
        └── app/
            ├── models/
            │   ├── book.ts          # Book, BookRequest interfaces
            │   ├── author.ts        # Author interface
            │   ├── category.ts      # Category interface
            │   └── page.ts          # Page<T> for Spring paginated responses
            ├── services/
            │   ├── book.service.ts
            │   ├── author.service.ts
            │   └── category.service.ts
            ├── components/
            │   ├── book-list/
            │   ├── book-detail/
            │   └── book-form/
            ├── app.routes.ts
            ├── app.component.ts
            └── app.config.ts
```

## Data Models

### Book (from backend BookResponse)

```typescript
export interface Book {
  id: number;
  title: string;
  description: string;
  price: number;
  isbn: string;
  publishedDate: string;
  authorId: number;
  authorName: string;
  categoryId: number;
  categoryName: string;
  stock: number;
}
```

### BookRequest (sent to backend on POST / PUT)

```typescript
export interface BookRequest {
  title: string;
  description: string;
  price: number;
  isbn: string;
  publishedDate: string;
  authorId: number;   // must be a valid author ID from /api/authors
  categoryId: number; // must be a valid category ID from /api/categories
  stock: number;
}
```

### Author, Category, Page

```typescript
export interface Author { id: number; name: string; bio: string; }
export interface Category { id: number; name: string; description: string; }
export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}
```

## Services

All services use `HttpClient` and `environment.backendBaseUrl`.

### BookService

```typescript
getBooks(page = 0, size = 100): Observable<Book[]>  // extracts .content from Page<Book>
getBook(id: number): Observable<Book>
addBook(request: BookRequest): Observable<Book>
updateBook(id: number, request: BookRequest): Observable<Book>
deleteBook(id: number): Observable<void>
```

### AuthorService

```typescript
getAuthors(): Observable<Author[]>  // calls GET /api/authors
```

### CategoryService

```typescript
getCategories(): Observable<Category[]>  // calls GET /api/categories
```

## Pages and Routes

| Route | Component | Description |
|-------|-----------|-------------|
| `/` | — | Redirects to `/books` |
| `/books` | `BookListComponent` | Grid of all books with category filter |
| `/books/new` | `BookFormComponent` | Add a new book |
| `/books/edit/:id` | `BookFormComponent` | Edit an existing book |
| `/books/:id` | `BookDetailComponent` | Full book info |

## Component Rules

- **book-list:** Fetches books from `BookService.getBooks()`. Fetches category names from `CategoryService`. Filters by `book.categoryName`. Delete triggers `BookService.deleteBook()`.
- **book-detail:** Fetches single book from `BookService.getBook(id)`. Delete navigates back to `/books`.
- **book-form:** Loads authors and categories into `<select>` dropdowns. Uses `[ngValue]` (not `[value]`) to preserve numeric IDs. Sends `BookRequest` to `addBook()` or `updateBook(id, request)`.

## Critical: Select Dropdown ID Binding

Always use `[ngValue]` on `<option>` elements — not `[value]`. `[value]` coerces to string, breaking the numeric `authorId`/`categoryId` in `BookRequest`.

```html
<!-- CORRECT -->
<select [(ngModel)]="formData.authorId" required>
  <option [ngValue]="null" disabled>Select author</option>
  <option *ngFor="let a of authors" [ngValue]="a.id">{{ a.name }}</option>
</select>

<!-- WRONG — sends "1" instead of 1 -->
<select [(ngModel)]="formData.authorId">
  <option [value]="a.id" *ngFor="let a of authors">{{ a.name }}</option>
</select>
```

## App Config

`app.config.ts` must include `provideHttpClient()`:

```typescript
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient()
  ]
};
```

## Environment Configuration

| File | When used | backendBaseUrl |
|------|-----------|----------------|
| `environment.ts` | `ng serve` (dev) | `http://localhost:8080/api` |
| `environment.prod.ts` | Docker build (`--configuration production`) | `http://backend:8080/api` |

`angular.json` production config must have `fileReplacements` swapping `environment.ts` → `environment.prod.ts`.

## Docker

```dockerfile
FROM node:18-alpine
RUN npm install -g @angular/cli@17 typescript
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 4200
CMD ["ng", "serve", "--host", "0.0.0.0", "--poll", "2000", "--configuration", "production"]
```

`.dockerignore`: `node_modules`, `dist`, `.angular`, `.vscode`, `*.md`

## Run Locally

```bash
cd obs-frontend/obs-frontend
npm install
ng serve          # http://localhost:4200
```

Requires backend running on port 8080.

## Git Workflow

Branch: `dev/frontend/OBS-*`

Commit style:
```
feat: add book list component
feat: implement book service
fix: use ngValue for select id binding
```

## Coding Standards

- Standalone components only — no NgModule
- Interfaces for all models — avoid `any`
- `CommonModule` and `RouterLink` imported per component where needed
- `FormsModule` imported in components using template-driven forms
- Services are `providedIn: 'root'`
- No business logic in components — delegate to services
