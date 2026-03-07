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
