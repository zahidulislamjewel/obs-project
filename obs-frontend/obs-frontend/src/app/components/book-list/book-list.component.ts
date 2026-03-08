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

  onImgError(event: Event): void {
    (event.target as HTMLImageElement).style.display = 'none';
  }
}
