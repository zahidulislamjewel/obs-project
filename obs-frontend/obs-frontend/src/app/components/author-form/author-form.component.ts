import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthorService } from '../../services/author.service';

@Component({
  selector: 'app-author-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './author-form.component.html',
  styleUrl: './author-form.component.css'
})
export class AuthorFormComponent {
  formData = { name: '', bio: '' };

  constructor(private authorService: AuthorService, private router: Router) {}

  onSubmit(): void {
    this.authorService.createAuthor(this.formData).subscribe(() => {
      this.router.navigate(['/authors']);
    });
  }
}
