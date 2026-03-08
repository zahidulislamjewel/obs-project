# Category Management + Book Cover Image Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add full category CRUD (dedicated pages + on-the-fly creation in book form) and a `coverImageUrl` field to books with display in list/detail views.

**Architecture:** Backend adds `CategoryRequest` DTO + `POST /api/categories` endpoint + `coverImageUrl` to Book entity/DTOs. Frontend mirrors the existing author management pattern for categories, adds inline category creation in the book form (same toggle pattern as author), and adds cover URL input + display.

**Tech Stack:** Java 21 / Spring Boot 3.4.3 / Angular 17 standalone components / template-driven forms / `ddl-auto=create` (schema auto-regenerated on restart)

---

## Task 1: Backend — CategoryRequest DTO

**Files:**
- Create: `obs-backend/src/main/java/com/obs/backend/dto/CategoryRequest.java`

**Step 1: Create the DTO**

```java
package com.obs.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;
}
```

**Step 2: Commit**
```bash
git add obs-backend/src/main/java/com/obs/backend/dto/CategoryRequest.java
git commit -m "feat: add CategoryRequest DTO"
```

---

## Task 2: Backend — CategoryService.createCategory()

**Files:**
- Modify: `obs-backend/src/main/java/com/obs/backend/service/CategoryService.java`
- Modify: `obs-backend/src/main/java/com/obs/backend/service/impl/CategoryServiceImpl.java`

**Step 1: Add method to service interface**

In `CategoryService.java`, add:
```java
import com.obs.backend.dto.CategoryRequest;

CategoryResponse createCategory(CategoryRequest request);
```

Full file after change:
```java
package com.obs.backend.service;

import com.obs.backend.dto.CategoryRequest;
import com.obs.backend.dto.CategoryResponse;
import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();
    CategoryResponse createCategory(CategoryRequest request);
}
```

**Step 2: Implement in CategoryServiceImpl**

Add `@Transactional` on the class (remove `readOnly = true` from class level, keep it only on `getAllCategories`), inject `CategoryRepository`, add `createCategory`:

```java
package com.obs.backend.service.impl;

import com.obs.backend.dto.CategoryRequest;
import com.obs.backend.dto.CategoryResponse;
import com.obs.backend.entity.Category;
import com.obs.backend.repository.CategoryRepository;
import com.obs.backend.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return CategoryResponse.from(categoryRepository.save(category));
    }
}
```

**Step 3: Commit**
```bash
git add obs-backend/src/main/java/com/obs/backend/service/CategoryService.java \
        obs-backend/src/main/java/com/obs/backend/service/impl/CategoryServiceImpl.java
git commit -m "feat: implement createCategory in CategoryService"
```

---

## Task 3: Backend — POST /api/categories endpoint

**Files:**
- Modify: `obs-backend/src/main/java/com/obs/backend/controller/CategoryController.java`

**Step 1: Add POST endpoint**

```java
package com.obs.backend.controller;

import com.obs.backend.dto.CategoryRequest;
import com.obs.backend.dto.CategoryResponse;
import com.obs.backend.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }
}
```

**Step 2: Verify backend compiles**
```bash
cd obs-backend && mvn compile -q
```
Expected: BUILD SUCCESS

**Step 3: Commit**
```bash
git add obs-backend/src/main/java/com/obs/backend/controller/CategoryController.java
git commit -m "feat: add POST /api/categories endpoint"
```

---

## Task 4: Backend — Add coverImageUrl to Book

**Files:**
- Modify: `obs-backend/src/main/java/com/obs/backend/entity/Book.java`
- Modify: `obs-backend/src/main/java/com/obs/backend/dto/BookRequest.java`
- Modify: `obs-backend/src/main/java/com/obs/backend/dto/BookResponse.java`
- Modify: `obs-backend/src/main/java/com/obs/backend/service/impl/BookServiceImpl.java`

**Step 1: Add field to Book entity**

In `Book.java`, add after the `stock` field:
```java
@Column(length = 2048)
private String coverImageUrl;
```

**Step 2: Add field to BookRequest**

In `BookRequest.java`, add at the end (before closing brace):
```java
private String coverImageUrl;
```

**Step 3: Add field to BookResponse**

In `BookResponse.java`, add the field:
```java
private String coverImageUrl;
```

And update the `from()` factory method — add `.coverImageUrl(book.getCoverImageUrl())` before `.build()`:
```java
public static BookResponse from(Book book) {
    return BookResponse.builder()
            .id(book.getId())
            .title(book.getTitle())
            .description(book.getDescription())
            .price(book.getPrice())
            .isbn(book.getIsbn())
            .publishedDate(book.getPublishedDate())
            .authorId(book.getAuthor().getId())
            .authorName(book.getAuthor().getName())
            .categoryId(book.getCategory().getId())
            .categoryName(book.getCategory().getName())
            .stock(book.getStock())
            .coverImageUrl(book.getCoverImageUrl())
            .build();
}
```

**Step 4: Update BookServiceImpl — map coverImageUrl in create and update**

In `createBook()`, add `.coverImageUrl(request.getCoverImageUrl())` to the builder:
```java
Book book = Book.builder()
        .title(request.getTitle())
        .description(request.getDescription())
        .price(request.getPrice())
        .isbn(request.getIsbn())
        .publishedDate(request.getPublishedDate())
        .author(author)
        .category(category)
        .stock(request.getStock())
        .coverImageUrl(request.getCoverImageUrl())
        .build();
```

In `updateBook()`, add after `book.setStock(request.getStock())`:
```java
book.setCoverImageUrl(request.getCoverImageUrl());
```

**Step 5: Verify backend compiles and tests pass**
```bash
cd obs-backend && mvn test -q
```
Expected: 18/18 tests pass, BUILD SUCCESS

**Step 6: Commit**
```bash
git add obs-backend/src/main/java/com/obs/backend/entity/Book.java \
        obs-backend/src/main/java/com/obs/backend/dto/BookRequest.java \
        obs-backend/src/main/java/com/obs/backend/dto/BookResponse.java \
        obs-backend/src/main/java/com/obs/backend/service/impl/BookServiceImpl.java
git commit -m "feat: add coverImageUrl field to Book entity and DTOs"
```

---

## Task 5: Frontend — CategoryRequest model + createCategory service

**Files:**
- Modify: `obs-frontend/obs-frontend/src/app/models/category.ts`
- Modify: `obs-frontend/obs-frontend/src/app/services/category.service.ts`

**Step 1: Add CategoryRequest interface**

In `category.ts`, append:
```typescript
export interface CategoryRequest {
  name: string;
  description: string;
}
```

**Step 2: Add createCategory() to CategoryService**

Read `category.service.ts` first, then add:
```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Category, CategoryRequest } from '../models/category';

@Injectable({ providedIn: 'root' })
export class CategoryService {
  private url = `${environment.backendBaseUrl}/categories`;

  constructor(private http: HttpClient) {}

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(this.url);
  }

  createCategory(request: CategoryRequest): Observable<Category> {
    return this.http.post<Category>(this.url, request);
  }
}
```

**Step 3: Commit**
```bash
git add obs-frontend/obs-frontend/src/app/models/category.ts \
        obs-frontend/obs-frontend/src/app/services/category.service.ts
git commit -m "feat: add CategoryRequest model and createCategory service method"
```

---

## Task 6: Frontend — CategoryFormComponent (dedicated create page)

**Files:**
- Create: `obs-frontend/obs-frontend/src/app/components/category-form/category-form.component.ts`
- Create: `obs-frontend/obs-frontend/src/app/components/category-form/category-form.component.html`
- Create: `obs-frontend/obs-frontend/src/app/components/category-form/category-form.component.css`

**Step 1: Create the component TS (mirrors AuthorFormComponent)**

```typescript
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CategoryService } from '../../services/category.service';

@Component({
  selector: 'app-category-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './category-form.component.html',
  styleUrl: './category-form.component.css'
})
export class CategoryFormComponent {
  formData = { name: '', description: '' };

  constructor(private categoryService: CategoryService, private router: Router) {}

  onSubmit(): void {
    this.categoryService.createCategory(this.formData).subscribe(() => {
      this.router.navigate(['/categories']);
    });
  }
}
```

**Step 2: Create the HTML template**

```html
<div class="form-page container">
  <div class="form-header">
    <a routerLink="/categories" class="back-link">
      <i class="fas fa-arrow-left"></i> Back
    </a>
    <div>
      <h1 class="form-title">New Category</h1>
      <p class="form-subtitle">Add a new book category</p>
    </div>
  </div>

  <div class="form-layout">
    <div class="form-card">
      <form (ngSubmit)="onSubmit()" #categoryForm="ngForm">
        <div class="form-grid">
          <div class="form-group full-width">
            <label for="name">Name <span class="req">*</span></label>
            <input id="name" name="name" [(ngModel)]="formData.name" required
              placeholder="e.g. Science Fiction" class="form-control">
          </div>
          <div class="form-group full-width">
            <label for="description">Description</label>
            <textarea id="description" name="description" [(ngModel)]="formData.description"
              rows="3" placeholder="What kind of books belong here?" class="form-control"></textarea>
          </div>
        </div>
        <div class="form-actions">
          <a routerLink="/categories" class="btn btn-ghost">Cancel</a>
          <button type="submit" [disabled]="categoryForm.invalid" class="btn btn-primary btn-lg">
            <i class="fas fa-plus"></i> Add Category
          </button>
        </div>
      </form>
    </div>
  </div>
</div>
```

**Step 3: Create empty CSS file**
```
/* category-form.component.css — inherits global styles */
```

**Step 4: Commit**
```bash
git add obs-frontend/obs-frontend/src/app/components/category-form/
git commit -m "feat: add CategoryFormComponent for dedicated category creation"
```

---

## Task 7: Frontend — CategoryListComponent (dedicated list page)

**Files:**
- Create: `obs-frontend/obs-frontend/src/app/components/category-list/category-list.component.ts`
- Create: `obs-frontend/obs-frontend/src/app/components/category-list/category-list.component.html`
- Create: `obs-frontend/obs-frontend/src/app/components/category-list/category-list.component.css`

**Step 1: Create the component TS**

```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CategoryService } from '../../services/category.service';
import { Category } from '../../models/category';

@Component({
  selector: 'app-category-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './category-list.component.html',
  styleUrl: './category-list.component.css'
})
export class CategoryListComponent implements OnInit {
  categories: Category[] = [];

  constructor(private categoryService: CategoryService) {}

  ngOnInit(): void {
    this.categoryService.getCategories().subscribe(c => this.categories = c);
  }
}
```

**Step 2: Create the HTML template**

```html
<div class="list-page container">
  <div class="page-header">
    <div>
      <h1 class="page-title">Categories</h1>
      <p class="page-subtitle">{{ categories.length }} categor{{ categories.length !== 1 ? 'ies' : 'y' }}</p>
    </div>
    <a routerLink="/categories/new" class="btn btn-accent">
      <i class="fas fa-plus"></i> New Category
    </a>
  </div>

  <div class="items-grid">
    <div class="item-card" *ngFor="let c of categories">
      <div class="item-icon"><i class="fas fa-tag"></i></div>
      <div class="item-info">
        <h3 class="item-name">{{ c.name }}</h3>
        <p class="item-bio">{{ c.description || 'No description' }}</p>
      </div>
    </div>
  </div>

  <div class="empty-state" *ngIf="categories.length === 0">
    <i class="fas fa-tag"></i>
    <p>No categories yet.</p>
    <a routerLink="/categories/new" class="btn btn-primary">Add the first one</a>
  </div>
</div>
```

**Step 3: Create empty CSS file**
```
/* category-list.component.css — inherits global styles */
```

**Step 4: Commit**
```bash
git add obs-frontend/obs-frontend/src/app/components/category-list/
git commit -m "feat: add CategoryListComponent"
```

---

## Task 8: Frontend — Wire routes and nav link

**Files:**
- Modify: `obs-frontend/obs-frontend/src/app/app.routes.ts`
- Modify: `obs-frontend/obs-frontend/src/app/app.component.html`

**Step 1: Add routes**

Full `app.routes.ts`:
```typescript
import { Routes } from '@angular/router';
import { BookListComponent } from './components/book-list/book-list.component';
import { BookDetailComponent } from './components/book-detail/book-detail.component';
import { BookFormComponent } from './components/book-form/book-form.component';
import { AuthorListComponent } from './components/author-list/author-list.component';
import { AuthorFormComponent } from './components/author-form/author-form.component';
import { CategoryListComponent } from './components/category-list/category-list.component';
import { CategoryFormComponent } from './components/category-form/category-form.component';

export const routes: Routes = [
  { path: '', redirectTo: '/books', pathMatch: 'full' },
  { path: 'books', component: BookListComponent },
  { path: 'books/new', component: BookFormComponent },
  { path: 'books/edit/:id', component: BookFormComponent },
  { path: 'books/:id', component: BookDetailComponent },
  { path: 'authors', component: AuthorListComponent },
  { path: 'authors/new', component: AuthorFormComponent },
  { path: 'categories', component: CategoryListComponent },
  { path: 'categories/new', component: CategoryFormComponent },
];
```

**Step 2: Add Categories nav link in app.component.html**

In the `<nav class="nav-links">` section, add after the Authors link:
```html
<a routerLink="/categories" class="nav-link">Categories</a>
```

**Step 3: Commit**
```bash
git add obs-frontend/obs-frontend/src/app/app.routes.ts \
        obs-frontend/obs-frontend/src/app/app.component.html
git commit -m "feat: add category routes and nav link"
```

---

## Task 9: Frontend — Inline category creation in book form

**Files:**
- Modify: `obs-frontend/obs-frontend/src/app/components/book-form/book-form.component.ts`
- Modify: `obs-frontend/obs-frontend/src/app/components/book-form/book-form.component.html`

**Step 1: Update book-form.component.ts**

Add `CategoryService` injection, `showCategoryForm`, `newCategoryData`, `toggleCategoryForm()`, `saveNewCategory()` — mirroring the existing author pattern:

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
    authorId: null,
    categoryId: null,
    stock: 0,
    coverImageUrl: ''
  };

  authors: Author[] = [];
  categories: Category[] = [];

  showAuthorForm = false;
  newAuthorData = { name: '', bio: '' };

  showCategoryForm = false;
  newCategoryData = { name: '', description: '' };

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
          stock: book.stock,
          coverImageUrl: book.coverImageUrl || ''
        };
      });
    }
  }

  toggleAuthorForm(): void {
    this.showAuthorForm = !this.showAuthorForm;
    this.newAuthorData = { name: '', bio: '' };
  }

  saveNewAuthor(): void {
    if (!this.newAuthorData.name.trim()) return;
    this.authorService.createAuthor(this.newAuthorData).subscribe(created => {
      this.authors.push(created);
      this.formData.authorId = created.id;
      this.showAuthorForm = false;
      this.newAuthorData = { name: '', bio: '' };
    });
  }

  toggleCategoryForm(): void {
    this.showCategoryForm = !this.showCategoryForm;
    this.newCategoryData = { name: '', description: '' };
  }

  saveNewCategory(): void {
    if (!this.newCategoryData.name.trim()) return;
    this.categoryService.createCategory(this.newCategoryData).subscribe(created => {
      this.categories.push(created);
      this.formData.categoryId = created.id;
      this.showCategoryForm = false;
      this.newCategoryData = { name: '', description: '' };
    });
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

Replace the category `<div class="form-group">` block (lines 54-60 in current file) with the inline-creation version, and add the cover URL field after description.

Replace the category section:
```html
          <div class="form-group">
            <label for="category">Category <span class="req">*</span></label>
            <div class="author-row">
              <select id="category" name="categoryId" [(ngModel)]="formData.categoryId" required class="form-control">
                <option [ngValue]="null" disabled>Select category</option>
                <option *ngFor="let c of categories" [ngValue]="c.id">{{ c.name }}</option>
              </select>
              <button type="button" class="btn btn-ghost btn-sm author-add-btn" (click)="toggleCategoryForm()">
                <i class="fas" [class.fa-plus]="!showCategoryForm" [class.fa-times]="showCategoryForm"></i>
                {{ showCategoryForm ? 'Cancel' : 'New' }}
              </button>
            </div>

            <div *ngIf="showCategoryForm" class="inline-author-form">
              <div class="form-group">
                <label>Name <span class="req">*</span></label>
                <input name="newCategoryName" [(ngModel)]="newCategoryData.name" placeholder="Category name" class="form-control">
              </div>
              <div class="form-group">
                <label>Description</label>
                <textarea name="newCategoryDesc" [(ngModel)]="newCategoryData.description" rows="2" placeholder="Short description (optional)" class="form-control"></textarea>
              </div>
              <button type="button" class="btn btn-primary btn-sm" (click)="saveNewCategory()">
                <i class="fas fa-check"></i> Save Category
              </button>
            </div>
          </div>
```

Add the cover image URL field after the description textarea block (after line 93):
```html
          <div class="form-group full-width">
            <label for="coverImageUrl">Cover Image URL</label>
            <input id="coverImageUrl" name="coverImageUrl" type="url"
              [(ngModel)]="formData.coverImageUrl"
              placeholder="https://example.com/cover.jpg" class="form-control">
          </div>
```

**Step 3: Commit**
```bash
git add obs-frontend/obs-frontend/src/app/components/book-form/
git commit -m "feat: add inline category creation and cover image URL to book form"
```

---

## Task 10: Frontend — Update Book model with coverImageUrl

**Files:**
- Modify: `obs-frontend/obs-frontend/src/app/models/book.ts`

**Step 1: Add coverImageUrl to both interfaces**

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
  coverImageUrl?: string;
}

export interface BookRequest {
  title: string;
  description: string;
  price: number;
  isbn: string;
  publishedDate: string;
  authorId: number | null;
  categoryId: number | null;
  stock: number;
  coverImageUrl?: string;
}
```

**Step 2: Commit**
```bash
git add obs-frontend/obs-frontend/src/app/models/book.ts
git commit -m "feat: add optional coverImageUrl to Book and BookRequest interfaces"
```

---

## Task 11: Frontend — Display cover image in book-detail and book-list

**Files:**
- Modify: `obs-frontend/obs-frontend/src/app/components/book-detail/book-detail.component.html`
- Modify: `obs-frontend/obs-frontend/src/app/components/book-list/book-list.component.html`

**Step 1: Add cover image to book-detail**

In `book-detail.component.html`, inside `<div class="detail-layout">` before `<div class="detail-info-col">`, add a cover column:
```html
    <!-- Left: Cover Image -->
    <div class="detail-cover-col">
      <img *ngIf="book.coverImageUrl" [src]="book.coverImageUrl" [alt]="book.title + ' cover'"
        class="detail-cover-img" (error)="onImgError($event)">
      <div *ngIf="!book.coverImageUrl" class="detail-cover-placeholder">
        <i class="fas fa-book"></i>
      </div>
    </div>
```

Also add `onImgError` to `book-detail.component.ts`:
```typescript
onImgError(event: Event): void {
  (event.target as HTMLImageElement).style.display = 'none';
}
```

**Step 2: Add thumbnail to book-list card**

In `book-list.component.html`, inside `<article class="book-card ...">` before `<div class="card-info">`, add:
```html
      <!-- Cover thumbnail -->
      <div class="card-cover">
        <img *ngIf="book.coverImageUrl" [src]="book.coverImageUrl" [alt]="book.title"
          class="card-cover-img" (error)="onImgError($event)">
        <div *ngIf="!book.coverImageUrl" class="card-cover-placeholder">
          <i class="fas fa-book"></i>
        </div>
      </div>
```

Also add `onImgError` to `book-list.component.ts`:
```typescript
onImgError(event: Event): void {
  (event.target as HTMLImageElement).style.display = 'none';
}
```

**Step 3: Commit**
```bash
git add obs-frontend/obs-frontend/src/app/components/book-detail/ \
        obs-frontend/obs-frontend/src/app/components/book-list/
git commit -m "feat: display cover image thumbnail in book list and detail"
```

---

## Task 12: Push, rebuild Docker, verify

**Step 1: Push all commits**
```bash
git push origin integration/OBS-INT-1
```

**Step 2: Rebuild and restart Docker stack**
```bash
docker compose -f infrastructure/docker-compose.yml up --build -d
```
Wait ~60 seconds for backend to start (Spring context + DB init).

**Step 3: Verify backend category endpoint**
```bash
curl -s -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Category","description":"Just a test"}' | python3 -m json.tool
```
Expected: `{"id": N, "name": "Test Category", "description": "Just a test"}`

**Step 4: Verify cover image field in book create**
```bash
curl -s -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","price":9.99,"isbn":"000-test-001","authorId":1,"categoryId":1,"stock":5,"coverImageUrl":"https://example.com/cover.jpg"}' | python3 -m json.tool
```
Expected: response includes `"coverImageUrl": "https://example.com/cover.jpg"`

**Step 5: Verify Angular UI**
- Open `http://localhost:4200`
- Click Categories nav link → list loads
- Click New Category → form works, saves, redirects back to list
- Click Add Book → Category dropdown has "New" button; inline form creates and auto-selects new category
- Add a book with a cover URL → thumbnail appears in book list card
- Click the book → detail page shows cover image on the left
