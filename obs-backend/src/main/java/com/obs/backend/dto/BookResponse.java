package com.obs.backend.dto;

import com.obs.backend.entity.Book;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    private Long authorId;
    private String authorName;
    private Long categoryId;
    private String categoryName;
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
                .authorId(book.getAuthor().getId())
                .authorName(book.getAuthor().getName())
                .categoryId(book.getCategory().getId())
                .categoryName(book.getCategory().getName())
                .stock(book.getStock())
                .coverImageUrl(book.getCoverImageUrl())
                .build();
    }
}
