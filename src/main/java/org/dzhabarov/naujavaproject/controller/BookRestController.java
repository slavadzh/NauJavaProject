package org.dzhabarov.naujavaproject.controller;

import org.dzhabarov.naujavaproject.dto.BookDTO;
import org.dzhabarov.naujavaproject.entity.Book;
import org.dzhabarov.naujavaproject.repository.BookRepository;
import org.dzhabarov.naujavaproject.service.BookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookRestController {

    private final BookService bookService;

    public BookRestController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/search/title")
    public List<BookDTO> findByTitle(@RequestParam String title) {
        return bookService.findByTitleContaining(title);
    }

    @GetMapping("/search/author")
    public List<BookDTO> findByAuthor(@RequestParam String name) {
        return bookService.findByAuthor(name);
    }
}