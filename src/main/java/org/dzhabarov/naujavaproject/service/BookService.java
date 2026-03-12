package org.dzhabarov.naujavaproject.service;

import org.dzhabarov.naujavaproject.entity.Book;

public interface BookService {

    void createBook(Long id, String title, String author);

    Book findById(Long id);

    void deleteById(Long id);

    void updateBook(Long id, String title, String author);
}