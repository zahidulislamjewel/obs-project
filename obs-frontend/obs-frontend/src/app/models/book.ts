export interface AuthorRef {
  id: number;
  name: string;
  bio?: string;
}

export interface CategoryRef {
  id: number;
  name: string;
  description?: string;
}

export interface Book {
  id: number;
  title: string;
  description: string;
  price: number;
  isbn: string;
  publishedDate: string;
  authors: AuthorRef[];
  categories: CategoryRef[];
  stock: number;
  coverImageUrl?: string;
}

export interface BookRequest {
  title: string;
  description: string;
  price: number;
  isbn: string;
  publishedDate: string;
  authorIds: number[];
  categoryIds: number[];
  stock: number;
  coverImageUrl?: string;
}
