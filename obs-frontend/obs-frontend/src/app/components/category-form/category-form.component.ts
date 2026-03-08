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
    this.categoryService.createCategory(this.formData).subscribe({
      next: () => this.router.navigate(['/categories']),
      error: (err) => console.error('Failed to create category', err)
    });
  }
}
