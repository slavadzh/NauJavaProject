package org.dzhabarov.naujavaproject.dto;

import lombok.Getter;
import lombok.Setter;
import org.dzhabarov.naujavaproject.entity.Author;

import java.util.List;

@Getter
@Setter
public class BookDTO {

    private String title;
    private Integer publicationYear;
    private String genre;
    private String isbn;
    private List<String> authorsNames;
}
