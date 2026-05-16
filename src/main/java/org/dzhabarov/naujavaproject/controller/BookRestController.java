package org.dzhabarov.naujavaproject.controller;

import org.dzhabarov.naujavaproject.dto.BookDTO;
import org.dzhabarov.naujavaproject.entity.Book;
import org.dzhabarov.naujavaproject.service.BookService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API каталога книг (поиск и CRUD для администратора)
 */
@RestController
@RequestMapping("/api/books")
public class BookRestController {

    private final BookService bookService;

    /**
     * @param bookService сервис каталога книг
     */
    public BookRestController(BookService bookService) {
        this.bookService = bookService;
    }

    /** Возвращает весь каталог с доступностью */
    @GetMapping
    public List<BookDTO> findAll() {
        return bookService.findAll();
    }

    /** Поиск по части названия */
    @GetMapping("/search/title")
    public List<BookDTO> findByTitle(@RequestParam String title) {
        return bookService.findByTitleContaining(title);
    }

    /** Поиск по имени автора */
    @GetMapping("/search/author")
    public List<BookDTO> findByAuthor(@RequestParam String name) {
        return bookService.findByAuthor(name);
    }

    /** Комбинированный поиск по названию, автору, жанру и году */
    @GetMapping("/search")
    public List<BookDTO> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer year) {
        return bookService.search(title, author, genre, year);
    }

    /** Создаёт книгу (только администратор) */
    @PostMapping("/admin")
    public Book create(@RequestBody Book book) {
        return bookService.create(book);
    }

    /** Обновляет книгу (только администратор) */
    @PutMapping("/admin/{id}")
    public Book update(@PathVariable Long id, @RequestBody Book book) {
        return bookService.update(id, book);
    }

    /** Удаляет книгу (только администратор) */
    @DeleteMapping("/admin/{id}")
    public void delete(@PathVariable Long id) {
        bookService.delete(id);
    }
}
