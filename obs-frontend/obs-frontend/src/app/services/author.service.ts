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

  createAuthor(request: { name: string; bio: string }): Observable<Author> {
    return this.http.post<Author>(this.baseUrl, request);
  }

  getAuthor(id: number): Observable<Author> {
    return this.http.get<Author>(`${this.baseUrl}/${id}`);
  }

  updateAuthor(id: number, request: { name: string; bio: string }): Observable<Author> {
    return this.http.put<Author>(`${this.baseUrl}/${id}`, request);
  }

  deleteAuthor(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
