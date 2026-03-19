package org.dzhabarov.naujavaproject.repository;

import org.dzhabarov.naujavaproject.entity.Book;

import java.util.List;

public interface BookRepositoryCustom {

    List<Book> findByTitleContainingCustom(String title);

    List<Book> findByAuthorNameCustom(String authorName);
}
