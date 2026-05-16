package org.dzhabarov.naujavaproject.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO книги для каталога и REST API
 */
@Getter
@Setter
public class BookDTO {

    private Long id;
    private String title;
    private Integer publicationYear;
    private String genre;
    private String isbn;
    /** Общее число экземпляров в фонде */
    private Integer totalCopies;
    /** Сколько экземпляров доступно прямо сейчас */
    private Integer availableCopies;
    private List<String> authorsNames;
}
