export interface Book {
  id: number;
  title: string;
  description: string;
  price: number;
  isbn: string;
  publishedDate: string;
  authorId: number;
  authorName: string;
  categoryId: number;
  categoryName: string;
  stock: number;
}

export interface BookRequest {
  title: string;
  description: string;
  price: number;
  isbn: string;
  publishedDate: string;
  authorId: number | null;
  categoryId: number | null;
  stock: number;
}
