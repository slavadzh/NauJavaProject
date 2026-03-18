package org.dzhabarov.naujavaproject.dao;


import org.dzhabarov.naujavaproject.entity.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookRepository implements CrudRepository<Book, Long> {

    private final List<Book> bookContainer;

    @Autowired
    public BookRepository(List<Book> bookContainer) {
        this.bookContainer = bookContainer;
    }

    @Override
    public void create(Book book) {
        bookContainer.add(book);
    }

    @Override
    public Book read(Long id) {
        for (Book book : bookContainer) {
            if (book.getId().equals(id)) {
                return book;
            }
        }
        return null;
    }

    @Override
    public void update(Book book) {
        for (Book b : bookContainer) {
            if (b.getId().equals(book.getId())) {
                b.setTitle(book.getTitle());
                b.setAuthor(book.getAuthor());
            }
        }
    }

    @Override
    public void delete(Long id) {
        bookContainer.removeIf(book -> book.getId().equals(id));
    }
}
