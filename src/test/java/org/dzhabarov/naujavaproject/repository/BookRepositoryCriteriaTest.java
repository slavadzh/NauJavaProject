package org.dzhabarov.naujavaproject.repository;

import org.dzhabarov.naujavaproject.entity.Author;
import org.dzhabarov.naujavaproject.entity.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookRepositoryCriteriaTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Test
    void testFindByTitleContainingCustom() {
        Book book = new Book();
        book.setTitle("Java Programming");
        book.setGenre("IT");
        bookRepository.save(book);

        List<Book> result = bookRepository.findByTitleContainingCustom("Java");

        assertFalse(result.isEmpty());
        assertTrue(result.get(0).getTitle().contains("Java"));
    }

    @Test
    void testFindByAuthorNameCustom() {
        Author author = new Author();
        author.setName("Joshua Bloch");
        authorRepository.save(author);

        Book book = new Book();
        book.setTitle("Effective Java");
        book.setGenre("IT");
        book.setAuthors(Set.of(author));
        bookRepository.save(book);

        List<Book> result = bookRepository.findByAuthorNameCustom("Joshua Bloch");

        assertFalse(result.isEmpty());
        assertEquals("Effective Java", result.get(0).getTitle());
    }
}