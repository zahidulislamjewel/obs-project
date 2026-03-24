package com.obs.backend.dto;

import com.obs.backend.entity.Book;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String isbn;
    private LocalDate publishedDate;
    private List<AuthorResponse> authors;
    private List<CategoryResponse> categories;
    private Integer stock;
    private String coverImageUrl;

    public static BookResponse from(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .description(book.getDescription())
                .price(book.getPrice())
                .isbn(book.getIsbn())
                .publishedDate(book.getPublishedDate())
                .authors(book.getBookAuthors().stream()
                        .map(ba -> AuthorResponse.from(ba.getAuthor()))
                        .toList())
                .categories(book.getBookCategories().stream()
                        .map(bc -> CategoryResponse.from(bc.getCategory()))
                        .toList())
                .stock(book.getStock())
                .coverImageUrl(book.getCoverImageUrl())
                .build();
    }
}
