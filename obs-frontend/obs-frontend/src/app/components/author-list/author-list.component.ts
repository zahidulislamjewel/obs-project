import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthorService } from '../../services/author.service';
import { Author } from '../../models/author';

@Component({
  selector: 'app-author-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './author-list.component.html',
  styleUrl: './author-list.component.css'
})
export class AuthorListComponent implements OnInit {
  authors: Author[] = [];

  constructor(private authorService: AuthorService) {}

  ngOnInit(): void {
    this.authorService.getAuthors().subscribe(a => this.authors = a);
  }

  deleteAuthor(id: number): void {
    if (!confirm('Delete this author? This cannot be undone.')) {
      return;
    }
    this.authorService.deleteAuthor(id).subscribe({
      next: () => {
        this.authors = this.authors.filter(a => a.id !== id);
      },
      error: (err) => {
        const message = err?.error?.message || err?.message || 'Failed to delete author.';
        alert(message);
      }
    });
  }
}
