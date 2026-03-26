package org.dzhabarov.naujavaproject.service;

import org.dzhabarov.naujavaproject.dto.BookDTO;
import org.dzhabarov.naujavaproject.mapper.BookMapper;
import org.dzhabarov.naujavaproject.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookService(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    public List<BookDTO> findByTitleContaining(String title) {
        return bookRepository.findByTitleContainingCustom(title).stream()
                .map(bookMapper::toBookDTO).collect(Collectors.toList());
    }

    public List<BookDTO> findByAuthor(String name) {
        return bookRepository.findByAuthorNameCustom(name).stream()
                .map(bookMapper::toBookDTO).collect(Collectors.toList());
    }

    public List<BookDTO> findAll() {
        return bookRepository.findAll().stream().map(bookMapper::toBookDTO).collect(Collectors.toList());
    }
}
