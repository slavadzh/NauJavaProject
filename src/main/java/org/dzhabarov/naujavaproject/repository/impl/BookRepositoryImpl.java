package org.dzhabarov.naujavaproject.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.dzhabarov.naujavaproject.entity.Book;
import org.dzhabarov.naujavaproject.repository.BookRepositoryCustom;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BookRepositoryImpl implements BookRepositoryCustom {

    private final EntityManager em;

    public BookRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<Book> findByTitleContainingCustom(String title) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Book> cq = cb.createQuery(Book.class);

        Root<Book> book = cq.from(Book.class);

        cq.select(book)
                .where(cb.like(book.get("title"), "%" + title + "%"));

        return em.createQuery(cq).getResultList();
    }

    @Override
    public List<Book> findByAuthorNameCustom(String authorName) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Book> cq = cb.createQuery(Book.class);

        Root<Book> book = cq.from(Book.class);
        Join<Object, Object> author = book.join("authors");

        cq.select(book)
                .where(cb.equal(author.get("name"), authorName));

        return em.createQuery(cq).getResultList();
    }
}
