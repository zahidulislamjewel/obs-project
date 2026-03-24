import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthorService } from '../../services/author.service';

@Component({
  selector: 'app-author-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './author-form.component.html',
  styleUrl: './author-form.component.css'
})
export class AuthorFormComponent implements OnInit {
  isEditMode = false;
  editId: number | null = null;
  formData = { name: '', bio: '' };

  constructor(
    private authorService: AuthorService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.editId = Number(id);
      this.authorService.getAuthor(this.editId).subscribe(author => {
        this.formData = { name: author.name, bio: author.bio || '' };
      });
    }
  }

  onSubmit(): void {
    if (this.isEditMode && this.editId != null) {
      this.authorService.updateAuthor(this.editId, this.formData).subscribe(() => {
        this.router.navigate(['/authors']);
      });
    } else {
      this.authorService.createAuthor(this.formData).subscribe(() => {
        this.router.navigate(['/authors']);
      });
    }
  }
}
