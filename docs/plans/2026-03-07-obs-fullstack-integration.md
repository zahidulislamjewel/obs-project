# OBS Full-Stack Integration Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Connect the Angular frontend to the Spring Boot backend, enable CORS, replace all mock data with real HTTP calls, and wire everything together with a root docker-compose.yml.

**Architecture:** Angular 17 → HttpClient → Spring Boot 3.4.3 → PostgreSQL 16. The browser makes API calls directly to `http://localhost:8080/api` (host-exposed port). Backend-to-Postgres communication uses Docker internal networking (`postgres:5432`). CORS allows `http://localhost:4200`.

**Tech Stack:** Angular 17 (standalone), Spring Boot 3.4.3, PostgreSQL 16, Docker Compose v2, Java 21

---

## Model Mapping

Backend `BookResponse` → Frontend `Book`:
- `authorName` → `author`
- `categoryName` → `category`
- `authorId`, `categoryId`, `isbn` added to frontend Book as optional fields

Frontend form → Backend `BookRequest`: look up `authorId`/`categoryId` from cached map built after fetching books. Author/Category dropdowns populated from `/api/authors` and `/api/categories`.

---

## Task 1: Create integration branches

**Files:** none (git ops only)

**Step 1:** Create backend branch
```bash
cd /home/jewel/Desktop/obs/obs-project/obs-backend
git checkout -b integration/frontend-connection
```

**Step 2:** Create frontend branch
```bash
cd /home/jewel/Desktop/obs/obs-project/obs-frontend
git checkout -b integration/backend-connection
```

---

## Task 2: Enable CORS in Spring Boot backend

**Files:**
- Create: `obs-backend/src/main/java/com/obs/backend/config/CorsConfig.java`

**Step 1:** Create the CORS configuration class
```java
package com.obs.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
```

**Step 2:** Commit on backend branch
```bash
cd obs-backend
git add src/main/java/com/obs/backend/config/CorsConfig.java
git commit -m "feat: enable cors for angular integration"
```

---

## Task 3: Add /api/authors and /api/categories endpoints

**Files:**
- Create: `obs-backend/src/main/java/com/obs/backend/dto/AuthorResponse.java`
- Create: `obs-backend/src/main/java/com/obs/backend/dto/CategoryResponse.java`
- Create: `obs-backend/src/main/java/com/obs/backend/controller/AuthorController.java`
- Create: `obs-backend/src/main/java/com/obs/backend/controller/CategoryController.java`

These endpoints let the frontend form populate author/category dropdowns.

**Step 1:** Create AuthorResponse DTO
```java
package com.obs.backend.dto;

import com.obs.backend.entity.Author;

public record AuthorResponse(Long id, String name) {
    public static AuthorResponse from(Author a) {
        return new AuthorResponse(a.getId(), a.getName());
    }
}
```

**Step 2:** Create CategoryResponse DTO
```java
package com.obs.backend.dto;

import com.obs.backend.entity.Category;

public record CategoryResponse(Long id, String name) {
    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName());
    }
}
```

**Step 3:** Create AuthorController
```java
package com.obs.backend.controller;

import com.obs.backend.dto.AuthorResponse;
import com.obs.backend.repository.AuthorRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorRepository authorRepository;

    public AuthorController(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @GetMapping
    public ResponseEntity<List<AuthorResponse>> getAll() {
        return ResponseEntity.ok(
            authorRepository.findAll().stream().map(AuthorResponse::from).toList()
        );
    }
}
```

**Step 4:** Create CategoryController
```java
package com.obs.backend.controller;

import com.obs.backend.dto.CategoryResponse;
import com.obs.backend.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        return ResponseEntity.ok(
            categoryRepository.findAll().stream().map(CategoryResponse::from).toList()
        );
    }
}
```

**Step 5:** Commit
```bash
cd obs-backend
git add src/main/java/com/obs/backend/dto/AuthorResponse.java \
        src/main/java/com/obs/backend/dto/CategoryResponse.java \
        src/main/java/com/obs/backend/controller/AuthorController.java \
        src/main/java/com/obs/backend/controller/CategoryController.java
git commit -m "feat: add author and category list endpoints"
```

---

## Task 4: Frontend — create environment file

**Files:**
- Create: `obs-frontend/obs-frontend/src/environments/environment.ts`

**Step 1:** Create environments dir and file
```typescript
export const environment = {
  backendBaseUrl: 'http://localhost:8080/api'
};
```

**Step 2:** Commit (will be part of larger frontend commit)

---

## Task 5: Frontend — enable HttpClient in app.config.ts

**Files:**
- Modify: `obs-frontend/obs-frontend/src/app/app.config.ts`

**Step 1:** Add `provideHttpClient()` to providers
```typescript
import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [provideRouter(routes), provideHttpClient()]
};
```

---

## Task 6: Frontend — extend Book model

**Files:**
- Modify: `obs-frontend/obs-frontend/src/app/models/book.ts`

Add `isbn`, `authorId`, `categoryId` as optional fields so the service can round-trip them through the form.

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
  isbn?: string;
  authorId?: number;
  categoryId?: number;
  coverUrl?: string;
  rating?: number;
  reviewCount?: number;
  badge?: 'bestseller' | 'new' | 'sale';
  originalPrice?: number;
}
```

---

## Task 7: Frontend — rewrite BookService with HttpClient

**Files:**
- Modify: `obs-frontend/obs-frontend/src/app/services/book.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Book } from '../models/book';
import { environment } from '../../environments/environment';

interface BookResponse {
  id: number; title: string; description: string; price: number;
  isbn: string; publishedDate: string; authorId: number; authorName: string;
  categoryId: number; categoryName: string; stock: number;
}
interface PageResponse { content: BookResponse[]; }
interface BookRequest {
  title: string; description: string; price: number; isbn: string;
  publishedDate: string; authorId: number; categoryId: number; stock: number;
}
export interface AuthorOption { id: number; name: string; }
export interface CategoryOption { id: number; name: string; }

@Injectable({ providedIn: 'root' })
export class BookService {
  private apiUrl = `${environment.backendBaseUrl}/books`;
  private authorsUrl = `${environment.backendBaseUrl}/authors`;
  private categoriesUrl = `${environment.backendBaseUrl}/categories`;
  private cachedBooks: Book[] = [];

  constructor(private http: HttpClient) {}

  private toBook(r: BookResponse): Book {
    return {
      id: r.id, title: r.title, description: r.description,
      price: r.price, isbn: r.isbn, publishedDate: r.publishedDate,
      author: r.authorName, authorId: r.authorId,
      category: r.categoryName, categoryId: r.categoryId,
      stock: r.stock,
    };
  }

  getBooks(): Observable<Book[]> {
    return this.http.get<PageResponse>(`${this.apiUrl}?size=100`).pipe(
      map(page => {
        this.cachedBooks = page.content.map(r => this.toBook(r));
        return this.cachedBooks;
      })
    );
  }

  getBook(id: number): Observable<Book> {
    return this.http.get<BookResponse>(`${this.apiUrl}/${id}`).pipe(
      map(r => this.toBook(r))
    );
  }

  addBook(book: Omit<Book, 'id'>): Observable<Book> {
    const req: BookRequest = {
      title: book.title, description: book.description || '',
      price: book.price, isbn: book.isbn || '',
      publishedDate: book.publishedDate,
      authorId: book.authorId ?? 1,
      categoryId: book.categoryId ?? 1,
      stock: book.stock,
    };
    return this.http.post<BookResponse>(this.apiUrl, req).pipe(map(r => this.toBook(r)));
  }

  updateBook(book: Book): Observable<Book> {
    const req: BookRequest = {
      title: book.title, description: book.description || '',
      price: book.price, isbn: book.isbn || '',
      publishedDate: book.publishedDate,
      authorId: book.authorId ?? 1,
      categoryId: book.categoryId ?? 1,
      stock: book.stock,
    };
    return this.http.put<BookResponse>(`${this.apiUrl}/${book.id}`, req).pipe(map(r => this.toBook(r)));
  }

  deleteBook(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getCategories(): string[] {
    return ['All', ...new Set(this.cachedBooks.map(b => b.category))];
  }

  getAuthors(): Observable<AuthorOption[]> {
    return this.http.get<AuthorOption[]>(this.authorsUrl);
  }

  getCategoryOptions(): Observable<CategoryOption[]> {
    return this.http.get<CategoryOption[]>(this.categoriesUrl);
  }
}
```

---

## Task 8: Frontend — update book-list.component.ts

Fix `getCategories()` to be called after books are loaded (not before).

**Files:**
- Modify: `obs-frontend/obs-frontend/src/app/components/book-list/book-list.component.ts`

Change `ngOnInit` to:
```typescript
ngOnInit(): void {
  this.bookService.getBooks().subscribe(books => {
    this.allBooks = books;
    this.filteredBooks = books;
    this.categories = this.bookService.getCategories();
  });
}
```
Remove the separate `this.categories = this.bookService.getCategories();` line.

---

## Task 9: Frontend — update book-form.component.ts and HTML

**Files:**
- Modify: `obs-frontend/obs-frontend/src/app/components/book-form/book-form.component.ts`
- Modify: `obs-frontend/obs-frontend/src/app/components/book-form/book-form.component.html`

**component.ts:** Load authors/categories via HTTP and expose them for the template. Add `isbn` to the book model. On submit, populate `authorId`/`categoryId` from selected dropdown.

```typescript
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BookService, AuthorOption, CategoryOption } from '../../services/book.service';
import { Book } from '../../models/book';

@Component({
  selector: 'app-book-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './book-form.component.html',
  styleUrl: './book-form.component.css'
})
export class BookFormComponent implements OnInit {
  isEditMode = false;
  authors: AuthorOption[] = [];
  categories: CategoryOption[] = [];
  book: Omit<Book, 'id'> & { id?: number } = {
    title: '', description: '', price: 0, author: '',
    category: '', publishedDate: '', stock: 0, coverUrl: '',
    isbn: '', authorId: undefined, categoryId: undefined
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookService: BookService
  ) {}

  ngOnInit(): void {
    this.bookService.getAuthors().subscribe(a => this.authors = a);
    this.bookService.getCategoryOptions().subscribe(c => this.categories = c);

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.bookService.getBook(Number(id)).subscribe(book => {
        if (book) this.book = { ...book };
      });
    }
  }

  onAuthorChange(authorId: number): void {
    const author = this.authors.find(a => a.id === +authorId);
    if (author) { this.book.author = author.name; this.book.authorId = author.id; }
  }

  onCategoryChange(categoryId: number): void {
    const category = this.categories.find(c => c.id === +categoryId);
    if (category) { this.book.category = category.name; this.book.categoryId = category.id; }
  }

  onSubmit(): void {
    if (this.isEditMode && this.book.id != null) {
      this.bookService.updateBook(this.book as Book).subscribe(() => {
        this.router.navigate(['/books', this.book.id]);
      });
    } else {
      this.bookService.addBook(this.book).subscribe(newBook => {
        this.router.navigate(['/books', newBook.id]);
      });
    }
  }
}
```

**HTML changes:** Replace static author input with a `<select>` from `authors`, replace static category `<select>` with dynamic `categories`, add ISBN field.

---

## Task 10: Create root docker-compose.yml

**Files:**
- Create: `obs-project/docker-compose.yml`

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: obs-postgres
    environment:
      POSTGRES_DB: obsdb
      POSTGRES_USER: obsuser
      POSTGRES_PASSWORD: obspass
    ports:
      - "5433:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U obsuser -d obsdb"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - obs-network

  backend:
    build:
      context: ./obs-backend
      dockerfile: Dockerfile
    container_name: obs-backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/obsdb
      SPRING_DATASOURCE_USERNAME: obsuser
      SPRING_DATASOURCE_PASSWORD: obspass
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - obs-network

  frontend:
    build:
      context: ./obs-frontend/obs-frontend
      dockerfile: Dockerfile
    container_name: obs-frontend
    ports:
      - "4200:4200"
    volumes:
      - ./obs-frontend/obs-frontend:/app
      - /app/node_modules
    environment:
      - CHOKIDAR_USEPOLLING=true
    depends_on:
      - backend
    networks:
      - obs-network

networks:
  obs-network:
    driver: bridge

volumes:
  postgres_data:
```

---

## Task 11: Commit all frontend changes

```bash
cd obs-frontend
git add obs-frontend/src/environments/environment.ts \
        obs-frontend/src/app/app.config.ts \
        obs-frontend/src/app/models/book.ts \
        obs-frontend/src/app/services/book.service.ts \
        obs-frontend/src/app/components/book-list/book-list.component.ts \
        obs-frontend/src/app/components/book-form/book-form.component.ts \
        obs-frontend/src/app/components/book-form/book-form.component.html
git commit -m "feat: connect angular book service to backend api"
```

---

## Task 12: Commit root docker-compose.yml

```bash
cd obs-project  # (no git repo here, this goes in neither sub-repo)
```
Note: `obs-project` is not a git repo itself. The docker-compose.yml at root is not committed to either sub-repo. Document its location for the user.

---

## Verification

Run `docker compose up --build` from `obs-project/` and verify:
1. `http://localhost:4200` loads book list from backend
2. Add book via form → appears in list
3. Edit book → changes persist
4. Delete book → removed from list
