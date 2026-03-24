# Design: Category Management + Book Cover Image URL

Date: 2026-03-08
Branch: integration/OBS-INT-1

## Problem

Two features are missing from the OBS full-stack application:

1. **Category management** — categories are read-only (seeded via SQL). Users cannot create new categories from the UI or on-the-fly while adding a book.
2. **Book cover image** — the Book entity and form have no cover image field, so books display without cover art.

## Solution

### Feature 1 — Category Management

Mirror the existing author management pattern exactly.

**Backend changes:**
- Add `CategoryRequest.java` DTO: `name` (required, @NotBlank), `description` (optional)
- Add `createCategory()` to `CategoryService` interface and `CategoryServiceImpl`
- Add `POST /api/categories` endpoint to `CategoryController` returning HTTP 201

**Frontend changes:**
- Add `CategoryRequest` interface to `category.ts` model
- Add `createCategory(request)` method to `category.service.ts`
- Create `CategoryListComponent` at route `/categories` — lists all categories with "New Category" button
- Create `CategoryFormComponent` at route `/categories/new` — form with name + description fields
- Add on-the-fly inline category creation to `book-form` (same toggle pattern as author inline form)
- Add Categories nav link in app navigation

### Feature 2 — Book Cover Image URL

Store and display a cover image URL string.

**Backend changes:**
- Add `coverImageUrl` VARCHAR column to `Book` entity (nullable, no constraint)
- Add optional `coverImageUrl` field to `BookRequest` DTO
- Add `coverImageUrl` field to `BookResponse` DTO
- `BookServiceImpl` maps the field through create and update operations

**Frontend changes:**
- Add optional `coverImageUrl: string` to `Book` interface
- Add optional `coverImageUrl: string` to `BookRequest` interface
- Add URL text input to book form (optional, labelled "Cover Image URL")
- Book detail page: show `<img>` tag if `coverImageUrl` is present, else show a placeholder
- Book list: show small thumbnail in each book card if URL is present, else show placeholder icon

## Data Flow

```
POST /api/categories
  Body: { name, description }
  Response: 201 { id, name, description }

POST /api/books (updated)
  Body: { title, description, price, isbn, publishedDate, authorId, categoryId, stock, coverImageUrl }
  Response: 201 { ...existing fields, coverImageUrl }
```

## Files to Change

| Layer | File | Change |
|-------|------|--------|
| Backend | `entity/Book.java` | Add `coverImageUrl` field |
| Backend | `dto/BookRequest.java` | Add optional `coverImageUrl` |
| Backend | `dto/BookResponse.java` | Add `coverImageUrl` |
| Backend | `service/impl/BookServiceImpl.java` | Map `coverImageUrl` in create/update |
| Backend | `dto/CategoryRequest.java` | New file |
| Backend | `service/CategoryService.java` | Add `createCategory()` |
| Backend | `service/impl/CategoryServiceImpl.java` | Implement `createCategory()` |
| Backend | `controller/CategoryController.java` | Add POST endpoint |
| Frontend | `models/category.ts` | Add `CategoryRequest` interface |
| Frontend | `models/book.ts` | Add `coverImageUrl` to `Book` and `BookRequest` |
| Frontend | `services/category.service.ts` | Add `createCategory()` |
| Frontend | `components/category-list/` | New component |
| Frontend | `components/category-form/` | New component |
| Frontend | `components/book-form/book-form.component.*` | Inline category creation + cover URL field |
| Frontend | `components/book-detail/book-detail.component.*` | Display cover image |
| Frontend | `components/book-list/book-list.component.*` | Show thumbnail |
| Frontend | `app.routes.ts` | Add `/categories` and `/categories/new` routes |
| Frontend | `app.component.*` | Add Categories nav link |
