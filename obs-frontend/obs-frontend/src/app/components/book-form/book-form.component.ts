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
    authorIds: [],
    categoryIds: [],
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
          authorIds: book.authors.map(a => a.id),
          categoryIds: book.categories.map(c => c.id),
          stock: book.stock,
          coverImageUrl: book.coverImageUrl || ''
        };
      });
    }
  }

  onPriceInput(event: Event): void {
    this.formData.price = parseFloat((event.target as HTMLInputElement).value) || 0;
  }

  onStockInput(event: Event): void {
    this.formData.stock = parseInt((event.target as HTMLInputElement).value) || 0;
  }

  onAuthorCheckboxChange(id: number, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    if (checked) {
      this.formData.authorIds = [...this.formData.authorIds, id];
    } else {
      this.formData.authorIds = this.formData.authorIds.filter(i => i !== id);
    }
  }

  onCategoryCheckboxChange(id: number, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    if (checked) {
      this.formData.categoryIds = [...this.formData.categoryIds, id];
    } else {
      this.formData.categoryIds = this.formData.categoryIds.filter(i => i !== id);
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
      this.formData.authorIds = [...this.formData.authorIds, created.id];
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
    this.categoryService.createCategory(this.newCategoryData).subscribe({
      next: (created) => {
        this.categories.push(created);
        this.formData.categoryIds = [...this.formData.categoryIds, created.id];
        this.showCategoryForm = false;
        this.newCategoryData = { name: '', description: '' };
      },
      error: (err) => console.error('Failed to create category', err)
    });
  }

  isFormValid(): boolean {
    return !!(
      this.formData.title.trim() &&
      this.formData.isbn.trim() &&
      Number(this.formData.price) > 0 &&
      this.formData.authorIds.length > 0 &&
      this.formData.categoryIds.length > 0 &&
      this.formData.stock >= 0
    );
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
