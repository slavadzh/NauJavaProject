package org.dzhabarov.naujavaproject.mapper;

import org.dzhabarov.naujavaproject.dto.BookDTO;
import org.dzhabarov.naujavaproject.entity.Author;
import org.dzhabarov.naujavaproject.entity.Book;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Преобразование сущности {@link Book} в {@link BookDTO}
 */
@Component
public class BookMapper {

    /**
     * Создаёт DTO книги с именами авторов
     *
     * @param book сущность книги
     * @return DTO для отображения в каталоге
     */
    public BookDTO toBookDTO(Book book) {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setId(book.getId());
        bookDTO.setTitle(book.getTitle());
        bookDTO.setGenre(book.getGenre());
        bookDTO.setIsbn(book.getIsbn());
        bookDTO.setPublicationYear(book.getPublicationYear());
        bookDTO.setTotalCopies(book.getTotalCopies());
        List<String> authorNames = book.getAuthors() == null
                ? Collections.emptyList()
                : book.getAuthors().stream().map(Author::getName).toList();
        bookDTO.setAuthorsNames(authorNames);
        return bookDTO;
    }
}
