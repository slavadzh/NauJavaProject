package org.dzhabarov.naujavaproject.repository;

import org.dzhabarov.naujavaproject.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(path = "books")
public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {

    List<Book> findByTitleContaining(String title);

    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.name = :name")
    List<Book> findByAuthor(String name);
}