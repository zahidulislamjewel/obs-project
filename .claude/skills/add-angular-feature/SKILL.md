---
name: add-angular-feature
description: Step-by-step guide for adding a new page or feature to the OBS Angular frontend. Covers model, service, component, routing, and template patterns. Use when adding a new page or extending an existing one.
---

# Add a New Angular Feature

Follow established patterns: standalone components, HttpClient services, environment-based URLs.

## Checklist

- [ ] Model interface added to `src/app/models/`
- [ ] Service created / updated in `src/app/services/`
- [ ] Component created in `src/app/components/`
- [ ] Route added to `app.routes.ts`
- [ ] Template uses correct field names (match backend response)
- [ ] Committed

## Step-by-Step

### 1. Add the Model

In `src/app/models/my-entity.ts`:

```typescript
export interface MyEntity {
  id: number;
  name: string;
  description: string;
}

export interface MyEntityRequest {
  name: string;
  description: string;
}
```

Mirror the backend DTO exactly. Never use `any`.

### 2. Create the Service

In `src/app/services/my-entity.service.ts`:

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MyEntity, MyEntityRequest } from '../models/my-entity';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class MyEntityService {
  private readonly baseUrl = `${environment.backendBaseUrl}/my-entities`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<MyEntity[]> {
    return this.http.get<MyEntity[]>(this.baseUrl);
  }

  getById(id: number): Observable<MyEntity> {
    return this.http.get<MyEntity>(`${this.baseUrl}/${id}`);
  }

  create(request: MyEntityRequest): Observable<MyEntity> {
    return this.http.post<MyEntity>(this.baseUrl, request);
  }

  update(id: number, request: MyEntityRequest): Observable<MyEntity> {
    return this.http.put<MyEntity>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
```

### 3. Create the Component

```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MyEntityService } from '../../services/my-entity.service';
import { MyEntity } from '../../models/my-entity';

@Component({
  selector: 'app-my-entity-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './my-entity-list.component.html',
  styleUrl: './my-entity-list.component.css'
})
export class MyEntityListComponent implements OnInit {
  entities: MyEntity[] = [];

  constructor(private myEntityService: MyEntityService) {}

  ngOnInit(): void {
    this.myEntityService.getAll().subscribe(data => this.entities = data);
  }
}
```

**For forms**, also import `FormsModule` and use `BookRequest`-style form data object.

### 4. Register the Route

In `src/app/app.routes.ts`:

```typescript
{ path: 'my-entities', component: MyEntityListComponent },
{ path: 'my-entities/:id', component: MyEntityDetailComponent },
{ path: 'my-entities/new', component: MyEntityFormComponent },
{ path: 'my-entities/edit/:id', component: MyEntityFormComponent },
```

### 5. Key Template Rules

**Displaying paginated backend data** (if endpoint returns `Page<T>`):

```typescript
// In service — unwrap .content
getAll(): Observable<MyEntity[]> {
  return this.http.get<Page<MyEntity>>(this.baseUrl).pipe(
    map(response => response.content)
  );
}
```

**Select dropdowns** — always use `[ngValue]` for numeric IDs:

```html
<select [(ngModel)]="formData.relatedEntityId" required>
  <option [ngValue]="null" disabled>Select one</option>
  <option *ngFor="let e of entities" [ngValue]="e.id">{{ e.name }}</option>
</select>
```

**Route links:**

```html
<a [routerLink]="['/my-entities', entity.id]">View</a>
<a [routerLink]="['/my-entities/edit', entity.id]">Edit</a>
```

### 6. Commit

```bash
git add src/app/models/my-entity.ts
git add src/app/services/my-entity.service.ts
git add src/app/components/my-entity-list/
git add src/app/app.routes.ts
git commit -m "feat: add my-entity list page and service"
```

## Common Mistakes to Avoid

| Mistake | Correct approach |
|---------|-----------------|
| `[value]="entity.id"` in selects | `[ngValue]="entity.id"` |
| Hardcoded API URL in service | `environment.backendBaseUrl` |
| `any` type in models | Explicit TypeScript interface |
| HTTP call in component | Move to service, inject service |
| Forgetting `provideHttpClient()` | Already in `app.config.ts` — do not remove |
| Using old `Book.author` field | Use `Book.authorName` |
| Using old `Book.category` field | Use `Book.categoryName` |
