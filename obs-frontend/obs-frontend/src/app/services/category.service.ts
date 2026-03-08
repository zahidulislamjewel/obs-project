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
