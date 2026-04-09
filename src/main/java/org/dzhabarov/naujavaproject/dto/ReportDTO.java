package org.dzhabarov.naujavaproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.dzhabarov.naujavaproject.entity.Book;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ReportDTO {

    private long usersCount;
    private List<Book> books;

    private long usersTime;
    private long booksTime;
    private long totalTime;
}
