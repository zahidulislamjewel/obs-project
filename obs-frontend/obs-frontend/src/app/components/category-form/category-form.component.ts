import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CategoryService } from '../../services/category.service';

@Component({
  selector: 'app-category-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './category-form.component.html',
  styleUrl: './category-form.component.css'
})
export class CategoryFormComponent implements OnInit {
  isEditMode = false;
  editId: number | null = null;
  formData = { name: '', description: '' };

  constructor(
    private categoryService: CategoryService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.editId = Number(id);
      this.categoryService.getCategory(this.editId).subscribe(category => {
        this.formData = { name: category.name, description: category.description || '' };
      });
    }
  }

  onSubmit(): void {
    if (this.isEditMode && this.editId != null) {
      this.categoryService.updateCategory(this.editId, this.formData).subscribe({
        next: () => this.router.navigate(['/categories']),
        error: (err) => console.error('Failed to update category', err)
      });
    } else {
      this.categoryService.createCategory(this.formData).subscribe({
        next: () => this.router.navigate(['/categories']),
        error: (err) => console.error('Failed to create category', err)
      });
    }
  }
}
