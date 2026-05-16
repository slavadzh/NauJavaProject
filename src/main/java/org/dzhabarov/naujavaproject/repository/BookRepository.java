package org.dzhabarov.naujavaproject.repository;

import org.dzhabarov.naujavaproject.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Репозиторий для работы с книгами в БД
 */
@RepositoryRestResource(path = "books")
public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {

    /**
     * Ищет книги, в названии которых встречается подстрока
     *
     * @param title часть названия
     * @return список найденных книг
     */
    List<Book> findByTitleContaining(String title);

    /**
     * Ищет книги по точному имени автора
     *
     * @param name имя автора
     * @return список книг автора
     */
    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.name = :name")
    List<Book> findByAuthor(String name);

    /** Книги с авторами (для админ-панели, без LazyInitializationException) */
    @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.authors ORDER BY b.id")
    List<Book> findAllWithAuthors();
}
