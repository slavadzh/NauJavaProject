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
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Test
    void testFindByTitleContaining() {
        Book book = new Book();
        book.setTitle("Java");
        book.setGenre("Java Programming");

        bookRepository.save(book);

        List<Book> result = bookRepository.findByTitleContaining("Java");

        assertFalse(result.isEmpty());
        assertEquals("Java", result.get(0).getTitle());
    }

    @Test
    void testFindByAuthor() {
        Author author = new Author();
        author.setName("Joshua Bloch");
        authorRepository.save(author);

        Book book = new Book();
        book.setTitle("Effective Java");
        book.setGenre("IT");
        book.setAuthors(Set.of(author));

        bookRepository.save(book);

        List<Book> result = bookRepository.findByAuthor("Joshua Bloch");

        assertFalse(result.isEmpty());
        assertEquals("Effective Java", result.get(0).getTitle());
    }
}