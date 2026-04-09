package org.dzhabarov.naujavaproject.service;

import org.dzhabarov.naujavaproject.dto.BookDTO;
import org.dzhabarov.naujavaproject.entity.Author;
import org.dzhabarov.naujavaproject.entity.Book;
import org.dzhabarov.naujavaproject.mapper.BookMapper;
import org.dzhabarov.naujavaproject.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    private Book book;
    private BookDTO bookDTO;

    @BeforeEach
    void setUp() {
        Author author = new Author();
        author.setName("Joshua Bloch");

        book = new Book();
        book.setTitle("Effective Java");
        book.setGenre("IT");
        book.setIsbn("123");
        book.setPublicationYear(2018);
        book.setAuthors(Set.of(author));

        bookDTO = new BookDTO();
        bookDTO.setTitle("Effective Java");
        bookDTO.setGenre("IT");
        bookDTO.setIsbn("123");
        bookDTO.setPublicationYear(2018);
        bookDTO.setAuthorsNames(List.of("Joshua Bloch"));
    }

    @Test
    void findAll_shouldReturnBooks() {
        when(bookRepository.findAll()).thenReturn(List.of(book));
        when(bookMapper.toBookDTO(book)).thenReturn(bookDTO);

        List<BookDTO> result = bookService.findAll();

        assertEquals(1, result.size());
        assertEquals("Effective Java", result.get(0).getTitle());
        assertEquals("Joshua Bloch", result.get(0).getAuthorsNames().get(0));

        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void findByTitleContaining_shouldReturnBooks() {
        when(bookRepository.findByTitleContainingCustom("Java"))
                .thenReturn(List.of(book));
        when(bookMapper.toBookDTO(book)).thenReturn(bookDTO);

        List<BookDTO> result = bookService.findByTitleContaining("Java");

        assertFalse(result.isEmpty());
        assertEquals("Effective Java", result.get(0).getTitle());

        verify(bookRepository).findByTitleContainingCustom("Java");
    }

    @Test
    void findByAuthor_shouldReturnEmptyList() {
        when(bookRepository.findByAuthorNameCustom("Unknown"))
                .thenReturn(List.of());

        List<BookDTO> result = bookService.findByAuthor("Unknown");

        assertTrue(result.isEmpty());

        verify(bookRepository).findByAuthorNameCustom("Unknown");
    }
}