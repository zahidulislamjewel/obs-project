# OBS Full-Stack Integration Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Integrate the Angular 17 frontend with the Spring Boot 3 backend into a single working full-stack system backed by PostgreSQL, all runnable via one `docker compose` command.

**Architecture:** Frontend calls backend REST API (`/api/books`, `/api/authors`, `/api/categories`) over HTTP. In dev, frontend runs at `localhost:4200` and backend at `localhost:8080`. In Docker, frontend container reaches backend via the Docker internal hostname `backend:8080`.

**Tech Stack:** Angular 17 (standalone, HttpClient), Spring Boot 3.4.3 (CORS config, two new REST controllers), PostgreSQL 16, Docker Compose v2.

---

## Critical Integration Notes (Read Before Starting)

### Data Model Mismatch
The frontend `Book` model uses `author: string` and `category: string`.
The backend `BookResponse` returns `authorId`, `authorName`, `categoryId`, `categoryName`.
The backend `BookRequest` requires `authorId: number` and `categoryId: number` — NOT plain strings.

**Resolution:** Update frontend `Book` model to match backend's response shape. Update templates accordingly. The book-form must send authorId/categoryId, not strings.

### Paginated GET /api/books
`GET /api/books` returns Spring `Page<BookResponse>`:
```json
{ "content": [...], "totalPages": 1, "totalElements": 11, ... }
```
Frontend service must extract `.content` from the response.

### Author/Category Dropdowns
The backend requires valid `authorId` and `categoryId` to create/update books. Backend does NOT yet expose `/api/authors` or `/api/categories` endpoints — these must be added (Tasks 1–2).

### Branch
All work goes on: `integration/OBS-INT-1`

---

## Task 1: Create Integration Branch

**Files:**
- (git only)

**Step 1: Create and checkout the branch**
```bash
cd /home/jewel/Desktop/obs/obs-project
git checkout -b integration/OBS-INT-1
```

**Step 2: Verify branch**
```bash
git branch --show-current
```
Expected output: `integration/OBS-INT-1`

---

## Task 2: Add Author List Endpoint to Backend

**Files:**
- Create: `obs-backend/src/main/java/com/obs/backend/dto/AuthorResponse.java`
- Create: `obs-backend/src/main/java/com/obs/backend/controller/AuthorController.java`

**Step 1: Create AuthorResponse DTO**

`obs-backend/src/main/java/com/obs/backend/dto/AuthorResponse.java`:
```java
package com.obs.backend.dto;

import com.obs.backend.entity.Author;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorResponse {
    private Long id;
    private String name;
    private String bio;

    public static AuthorResponse from(Author author) {
        return AuthorResponse.builder()
                .id(author.getId())
                .name(author.getName())
                .bio(author.getBio())
                .build();
    }
}
```

**Step 2: Create AuthorController**

`obs-backend/src/main/java/com/obs/backend/controller/AuthorController.java`:
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
    public ResponseEntity<List<AuthorResponse>> getAllAuthors() {
        List<AuthorResponse> authors = authorRepository.findAll()
                .stream()
                .map(AuthorResponse::from)
                .toList();
        return ResponseEntity.ok(authors);
    }
}
```

**Step 3: Commit**
```bash
cd /home/jewel/Desktop/obs/obs-project
git add obs-backend/src/main/java/com/obs/backend/dto/AuthorResponse.java
git add obs-backend/src/main/java/com/obs/backend/controller/AuthorController.java
git commit -m "feat: add GET /api/authors endpoint"
```

---

## Task 3: Add Category List Endpoint to Backend

**Files:**
- Create: `obs-backend/src/main/java/com/obs/backend/dto/CategoryResponse.java`
- Create: `obs-backend/src/main/java/com/obs/backend/controller/CategoryController.java`

**Step 1: Create CategoryResponse DTO**

`obs-backend/src/main/java/com/obs/backend/dto/CategoryResponse.java`:
```java
package com.obs.backend.dto;

import com.obs.backend.entity.Category;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
```

**Step 2: Create CategoryController**

`obs-backend/src/main/java/com/obs/backend/controller/CategoryController.java`:
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
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::from)
                .toList();
        return ResponseEntity.ok(categories);
    }
}
```

**Step 3: Commit**
```bash
git add obs-backend/src/main/java/com/obs/backend/dto/CategoryResponse.java
git add obs-backend/src/main/java/com/obs/backend/controller/CategoryController.java
git commit -m "feat: add GET /api/categories endpoint"
```

---

## Task 4: Enable CORS in Backend

**Files:**
- Create: `obs-backend/src/main/java/com/obs/backend/config/CorsConfig.java`

**Step 1: Create CorsConfig**

`obs-backend/src/main/java/com/obs/backend/config/CorsConfig.java`:
```java
package com.obs.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
```

**Step 2: Commit**
```bash
git add obs-backend/src/main/java/com/obs/backend/config/CorsConfig.java
git commit -m "feat: enable cors for frontend integration"
```

---

## Task 5: Configure Angular Environment

**Files:**
- Create: `obs-frontend/obs-frontend/src/environments/environment.ts`
- Create: `obs-frontend/obs-frontend/src/environments/environment.prod.ts`
- Modify: `obs-frontend/obs-frontend/angular.json` — add fileReplacements under production config

**Step 1: Create development environment file**

`obs-frontend/obs-frontend/src/environments/environment.ts`:
```typescript
export const environment = {
  production: false,
  backendBaseUrl: 'http://localhost:8080/api'
};
```

**Step 2: Create production environment file**

`obs-frontend/obs-frontend/src/environments/environment.prod.ts`:
```typescript
export const environment = {
  production: true,
  backendBaseUrl: 'http://backend:8080/api'
};
```

**Step 3: Add fileReplacements to angular.json**

In `obs-frontend/obs-frontend/angular.json`, under `projects.obs-frontend.architect.build.configurations.production`, add:
```json
"fileReplacements": [
  {
    "replace": "src/environments/environment.ts",
    "with": "src/environments/environment.prod.ts"
  }
]
```

The full production configuration block becomes:
```json
"production": {
  "budgets": [
    {
      "type": "initial",
      "maximumWarning": "500kb",
      "maximumError": "1mb"
    },
    {
      "type": "anyComponentStyle",
      "maximumWarning": "8kb",
      "maximumError": "16kb"
    }
  ],
  "outputHashing": "all",
  "fileReplacements": [
    {
      "replace": "src/environments/environment.ts",
      "with": "src/environments/environment.prod.ts"
    }
  ]
}
```

**Step 4: Commit**
```bash
git add obs-frontend/obs-frontend/src/environments/
git add obs-frontend/obs-frontend/angular.json
git commit -m "feat: configure angular environment to point to backend api"
```

---

## Task 6: Update Frontend Book Model and Add Supporting Models

**Files:**
- Modify: `obs-frontend/obs-frontend/src/app/models/book.ts`
- Create: `obs-frontend/obs-frontend/src/app/models/author.ts`
- Create: `obs-frontend/obs-frontend/src/app/models/category.ts`
- Create: `obs-frontend/obs-frontend/src/app/models/page.ts`

**Step 1: Update Book model to match backend BookResponse**

`obs-frontend/obs-frontend/src/app/models/book.ts`:
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

export interface BookRequest {
  title: string;
  description: string;
  price: number;
  isbn: string;
  publishedDate: string;
  authorId: number;
  categoryId: number;
  stock: number;
}
```

**Step 2: Create Author model**

`obs-frontend/obs-frontend/src/app/models/author.ts`:
```typescript
export interface Author {
  id: number;
  name: string;
  bio: string;
}
```

**Step 3: Create Category model**

`obs-frontend/obs-frontend/src/app/models/category.ts`:
```typescript
export interface Category {
  id: number;
  name: string;
  description: string;
}
```

**Step 4: Create Page model (Spring paginated response)**

`obs-frontend/obs-frontend/src/app/models/page.ts`:
```typescript
export interface Page<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}
```

**Step 5: Commit**
```bash
git add obs-frontend/obs-frontend/src/app/models/
git commit -m "feat: update book model and add author, category, page models"
```

---

## Task 7: Wire HttpClient in Angular App Config

**Files:**
- Modify: `obs-frontend/obs-frontend/src/app/app.config.ts`

**Step 1: Add provideHttpClient to app config**

`obs-frontend/obs-frontend/src/app/app.config.ts`:
```typescript
import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient()
  ]
};
```

**Step 2: Commit**
```bash
git add obs-frontend/obs-frontend/src/app/app.config.ts
git commit -m "feat: wire HttpClient provider in angular app config"
```

---

## Task 8: Replace Mock BookService with Real HttpClient Calls

**Files:**
- Modify: `obs-frontend/obs-frontend/src/app/services/book.service.ts`

**Step 1: Rewrite BookService**

`obs-frontend/obs-frontend/src/app/services/book.service.ts`:
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { Book, BookRequest } from '../models/book';
import { Page } from '../models/page';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BookService {
  private readonly baseUrl = `${environment.backendBaseUrl}/books`;

  constructor(private http: HttpClient) {}

  getBooks(page = 0, size = 100): Observable<Book[]> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<Book>>(this.baseUrl, { params }).pipe(
      map(response => response.content)
    );
  }

  getBook(id: number): Observable<Book> {
    return this.http.get<Book>(`${this.baseUrl}/${id}`);
  }

  addBook(request: BookRequest): Observable<Book> {
    return this.http.post<Book>(this.baseUrl, request);
  }

  updateBook(id: number, request: BookRequest): Observable<Book> {
    return this.http.put<Book>(`${this.baseUrl}/${id}`, request);
  }

  deleteBook(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
```

**Step 2: Commit**
```bash
git add obs-frontend/obs-frontend/src/app/services/book.service.ts
git commit -m "feat: connect angular book service to backend api"
```

---

## Task 9: Add Author and Category Services

**Files:**
- Create: `obs-frontend/obs-frontend/src/app/services/author.service.ts`
- Create: `obs-frontend/obs-frontend/src/app/services/category.service.ts`

**Step 1: Create AuthorService**

`obs-frontend/obs-frontend/src/app/services/author.service.ts`:
```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Author } from '../models/author';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthorService {
  private readonly baseUrl = `${environment.backendBaseUrl}/authors`;

  constructor(private http: HttpClient) {}

  getAuthors(): Observable<Author[]> {
    return this.http.get<Author[]>(this.baseUrl);
  }
}
```

**Step 2: Create CategoryService**

`obs-frontend/obs-frontend/src/app/services/category.service.ts`:
```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Category } from '../models/category';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private readonly baseUrl = `${environment.backendBaseUrl}/categories`;

  constructor(private http: HttpClient) {}

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(this.baseUrl);
  }
}
```

**Step 3: Commit**
```bash
git add obs-frontend/obs-frontend/src/app/services/
git commit -m "feat: add author and category services"
```

---

## Task 10: Update BookListComponent

**Files:**
- Modify: `obs-frontend/obs-frontend/src/app/components/book-list/book-list.component.ts`
- Modify: `obs-frontend/obs-frontend/src/app/components/book-list/book-list.component.html`

**Context:** The old component used `book.author` and `book.category`; the new model uses `book.authorName` and `book.categoryName`. The `getCategories()` method was on `BookService` using in-memory data; now categories come from the backend. The `filterByCategory()` must filter on `book.categoryName`.

**Step 1: Update book-list.component.ts**

`obs-frontend/obs-frontend/src/app/components/book-list/book-list.component.ts`:
```typescript
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { BookService } from '../../services/book.service';
import { CategoryService } from '../../services/category.service';
import { Book } from '../../models/book';

@Component({
  selector: 'app-book-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './book-list.component.html',
  styleUrl: './book-list.component.css'
})
export class BookListComponent implements OnInit {
  allBooks: Book[] = [];
  filteredBooks: Book[] = [];
  categoryNames: string[] = [];
  activeCategory = 'All';

  constructor(
    private bookService: BookService,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    this.bookService.getBooks().subscribe(books => {
      this.allBooks = books;
      this.filteredBooks = books;
    });
    this.categoryService.getCategories().subscribe(categories => {
      this.categoryNames = ['All', ...categories.map(c => c.name)];
    });
  }

  filterByCategory(cat: string): void {
    this.activeCategory = cat;
    this.filteredBooks = cat === 'All'
      ? this.allBooks
      : this.allBooks.filter(b => b.categoryName === cat);
  }

  deleteBook(id: number, event: Event): void {
    event.preventDefault();
    event.stopPropagation();
    if (confirm('Delete this book?')) {
      this.bookService.deleteBook(id).subscribe(() => {
        this.allBooks = this.allBooks.filter(b => b.id !== id);
        this.filterByCategory(this.activeCategory);
      });
    }
  }

  getCoverUrl(book: Book): string {
    return `https://placehold.co/200x280/0D5C63/ffffff?text=${encodeURIComponent(book.title.substring(0, 15))}`;
  }
}
```

**Step 2: Update book-list.component.html**

In the template, replace every occurrence of:
- `book.author` → `book.authorName`
- `book.category` → `book.categoryName`
- `categories` (the old array) → `categoryNames`
- Remove any references to `book.rating`, `book.reviewCount`, `book.badge`, `book.originalPrice`, `book.coverUrl` (these fields no longer exist on the model). Replace `getCoverUrl(book)` call with the placeholder URL approach already in the updated component, or simply remove cover images and badges from the template.

Read the current template first:

File: `obs-frontend/obs-frontend/src/app/components/book-list/book-list.component.html`

Then replace ALL instances of:
- `book.author` → `book.authorName`
- `book.category` → `book.categoryName`
- `categories` (loop variable for category filter) → `categoryNames`
- Remove star ratings, badges, cover image blocks that depend on removed fields

**Step 3: Commit**
```bash
git add obs-frontend/obs-frontend/src/app/components/book-list/
git commit -m "feat: update book-list to use real api data"
```

---

## Task 11: Update BookDetailComponent

**Files:**
- Modify: `obs-frontend/obs-frontend/src/app/components/book-detail/book-detail.component.ts`
- Modify: `obs-frontend/obs-frontend/src/app/components/book-detail/book-detail.component.html`

**Step 1: Update book-detail.component.ts**

`obs-frontend/obs-frontend/src/app/components/book-detail/book-detail.component.ts`:
```typescript
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { BookService } from '../../services/book.service';
import { Book } from '../../models/book';

@Component({
  selector: 'app-book-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './book-detail.component.html',
  styleUrl: './book-detail.component.css'
})
export class BookDetailComponent implements OnInit {
  book: Book | undefined;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookService: BookService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.bookService.getBook(id).subscribe(book => this.book = book);
  }

  deleteBook(): void {
    if (this.book && confirm('Delete this book?')) {
      this.bookService.deleteBook(this.book.id).subscribe(() => {
        this.router.navigate(['/books']);
      });
    }
  }
}
```

**Step 2: Update book-detail.component.html**

Read the current template, then replace:
- `book.author` → `book.authorName`
- `book.category` → `book.categoryName`
- Remove star ratings, cover image, badge references

**Step 3: Commit**
```bash
git add obs-frontend/obs-frontend/src/app/components/book-detail/
git commit -m "feat: update book-detail to use real api data"
```

---

## Task 12: Update BookFormComponent

**Files:**
- Modify: `obs-frontend/obs-frontend/src/app/components/book-form/book-form.component.ts`
- Modify: `obs-frontend/obs-frontend/src/app/components/book-form/book-form.component.html`

**Context:** The form previously had free-text fields for `author` and `category`. Now it must show dropdowns populated from `/api/authors` and `/api/categories`. The form submits `BookRequest` (authorId, categoryId, not strings). The `updateBook()` signature changed: it now takes `(id: number, request: BookRequest)`.

**Step 1: Update book-form.component.ts**

`obs-frontend/obs-frontend/src/app/components/book-form/book-form.component.ts`:
```typescript
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BookService } from '../../services/book.service';
import { AuthorService } from '../../services/author.service';
import { CategoryService } from '../../services/category.service';
import { BookRequest } from '../../models/book';
import { Author } from '../../models/author';
import { Category } from '../../models/category';

@Component({
  selector: 'app-book-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './book-form.component.html',
  styleUrl: './book-form.component.css'
})
export class BookFormComponent implements OnInit {
  isEditMode = false;
  editId: number | null = null;

  formData: BookRequest = {
    title: '',
    description: '',
    price: 0,
    isbn: '',
    publishedDate: '',
    authorId: 0,
    categoryId: 0,
    stock: 0
  };

  authors: Author[] = [];
  categories: Category[] = [];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookService: BookService,
    private authorService: AuthorService,
    private categoryService: CategoryService
  ) {}

  ngOnInit(): void {
    this.authorService.getAuthors().subscribe(a => this.authors = a);
    this.categoryService.getCategories().subscribe(c => this.categories = c);

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.editId = Number(id);
      this.bookService.getBook(this.editId).subscribe(book => {
        this.formData = {
          title: book.title,
          description: book.description,
          price: book.price,
          isbn: book.isbn,
          publishedDate: book.publishedDate,
          authorId: book.authorId,
          categoryId: book.categoryId,
          stock: book.stock
        };
      });
    }
  }

  onSubmit(): void {
    if (this.isEditMode && this.editId != null) {
      this.bookService.updateBook(this.editId, this.formData).subscribe(updated => {
        this.router.navigate(['/books', updated.id]);
      });
    } else {
      this.bookService.addBook(this.formData).subscribe(created => {
        this.router.navigate(['/books', created.id]);
      });
    }
  }
}
```

**Step 2: Update book-form.component.html**

Read the current template. Replace the free-text `author` and `category` input fields with `<select>` dropdowns bound to `formData.authorId` and `formData.categoryId`:

```html
<!-- Replace author text input with: -->
<select id="author" name="author" [(ngModel)]="formData.authorId" required>
  <option [value]="0" disabled>Select author</option>
  <option *ngFor="let a of authors" [value]="a.id">{{ a.name }}</option>
</select>

<!-- Replace category text input with: -->
<select id="category" name="category" [(ngModel)]="formData.categoryId" required>
  <option [value]="0" disabled>Select category</option>
  <option *ngFor="let c of categories" [value]="c.id">{{ c.name }}</option>
</select>
```

Also: remove any `coverUrl`, `rating`, `badge`, `originalPrice` form fields if present.
Also: ensure `isbn` field exists (it was missing from the original form — add it).

**Step 3: Commit**
```bash
git add obs-frontend/obs-frontend/src/app/components/book-form/
git commit -m "feat: update book-form with author/category dropdowns and isbn field"
```

---

## Task 13: Create Unified Docker Compose

**Files:**
- Create: `docker/docker-compose.yml`

**Context:** The CLAUDE.md specifies running from `docker/docker-compose.yml`. The backend already has its own `docker-compose.yml` at `obs-backend/docker-compose.yml`. The unified one must coordinate all three services. The frontend container needs to reach the backend via `http://backend:8080` inside Docker, which is what `environment.prod.ts` already has.

**Step 1: Create the docker directory if it doesn't exist**
```bash
mkdir -p /home/jewel/Desktop/obs/obs-project/docker
```

**Step 2: Create docker/docker-compose.yml**

`docker/docker-compose.yml`:
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
      context: ../obs-backend
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
      context: ../obs-frontend/obs-frontend
      dockerfile: Dockerfile
    container_name: obs-frontend
    ports:
      - "4200:4200"
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

**Step 3: Commit**
```bash
git add docker/docker-compose.yml
git commit -m "chore: add full stack docker compose"
```

---

## Task 14: Verify Book-List and Book-Detail HTML Templates

**Files:**
- Read and verify: `obs-frontend/obs-frontend/src/app/components/book-list/book-list.component.html`
- Read and verify: `obs-frontend/obs-frontend/src/app/components/book-detail/book-detail.component.html`

**Step 1: Read both templates**

Check for any remaining references to old fields: `book.author`, `book.category`, `book.coverUrl`, `book.rating`, `book.badge`, `book.originalPrice`, `book.reviewCount`.

**Step 2: Fix any remaining references**

Replace:
- `book.author` → `book.authorName`
- `book.category` → `book.categoryName`
- Remove any `*ngIf` or interpolation referencing `book.coverUrl`, `book.rating`, `book.badge`, `book.originalPrice`, `book.reviewCount`

**Step 3: Commit if any fixes were needed**
```bash
git add obs-frontend/obs-frontend/src/app/components/
git commit -m "fix: update templates to use new book model fields"
```

---

## Task 15: Verify Book-Form HTML Template

**Files:**
- Read and verify: `obs-frontend/obs-frontend/src/app/components/book-form/book-form.component.html`

**Step 1: Read the template**

Confirm:
- `isbn` field exists and is bound to `formData.isbn`
- `author` dropdown is a `<select>` bound to `formData.authorId` (not a text input)
- `category` dropdown is a `<select>` bound to `formData.categoryId` (not a text input)
- No references to removed fields (`coverUrl`, `rating`, `badge`)

**Step 2: Fix any issues found and commit if needed**
```bash
git add obs-frontend/obs-frontend/src/app/components/book-form/
git commit -m "fix: ensure book-form template binds to correct formData fields"
```

---

## Task 16: Final Verification Checklist

Run through this checklist before declaring done:

**Backend:**
- [ ] `GET /api/books` — returns paginated response with `.content` array
- [ ] `GET /api/books/{id}` — returns single book with `authorName`, `categoryName`, `authorId`, `categoryId`
- [ ] `POST /api/books` — accepts `BookRequest` with `authorId`, `categoryId`
- [ ] `PUT /api/books/{id}` — same as POST
- [ ] `DELETE /api/books/{id}` — returns 204
- [ ] `GET /api/authors` — returns list of authors
- [ ] `GET /api/categories` — returns list of categories
- [ ] CORS allows `http://localhost:4200`

**Frontend:**
- [ ] `environment.ts` has `backendBaseUrl: 'http://localhost:8080/api'`
- [ ] `environment.prod.ts` has `backendBaseUrl: 'http://backend:8080/api'`
- [ ] `app.config.ts` includes `provideHttpClient()`
- [ ] `book.service.ts` uses HttpClient, not `of()`
- [ ] Book model has `authorId`, `authorName`, `categoryId`, `categoryName` (not `author`/`category`)
- [ ] `book-list` loads from API, filters by `categoryName`
- [ ] `book-form` has `<select>` dropdowns for author and category
- [ ] `book-form` sends `authorId`/`categoryId` in the request body
- [ ] `isbn` field present in the form

**Docker:**
- [ ] `docker/docker-compose.yml` defines postgres, backend, frontend services
- [ ] All three services on the same `obs-network`
- [ ] Frontend has `depends_on: backend`
- [ ] Backend has `depends_on: postgres` with health check condition

**Git:**
- [ ] All commits on branch `integration/OBS-INT-1`
- [ ] Branch has NOT been pushed (user will review first)
