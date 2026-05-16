package org.dzhabarov.naujavaproject.repository;

import org.dzhabarov.naujavaproject.entity.Book;

import java.util.List;

/**
 * Дополнительные методы поиска книг через Criteria API
 */
public interface BookRepositoryCustom {

    /**
     * Поиск книг по части названия (кастомный запрос)
     *
     * @param title часть названия
     * @return список книг
     */
    List<Book> findByTitleContainingCustom(String title);

    /**
     * Поиск книг по имени автора (кастомный запрос)
     *
     * @param authorName имя автора
     * @return список книг
     */
    List<Book> findByAuthorNameCustom(String authorName);
}
