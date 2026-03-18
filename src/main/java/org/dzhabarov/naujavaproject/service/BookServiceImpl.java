package org.dzhabarov.naujavaproject.service;

import org.dzhabarov.naujavaproject.dao.BookRepository;
import org.dzhabarov.naujavaproject.entity.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Autowired
    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void createBook(Long id, String title, String author) {

        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setAuthor(author);

        bookRepository.create(book);
    }

    @Override
    public Book findById(Long id) {
        return bookRepository.read(id);
    }

    @Override
    public void deleteById(Long id) {
        bookRepository.delete(id);
    }

    @Override
    public void updateBook(Long id, String title, String author) {

        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setAuthor(author);

        bookRepository.update(book);
    }
}
