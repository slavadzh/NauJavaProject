package org.dzhabarov.naujavaproject.repository;

import org.dzhabarov.naujavaproject.entity.Book;
import org.dzhabarov.naujavaproject.entity.Loan;
import org.dzhabarov.naujavaproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с выдачами книг в БД
 */
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /**
     * Считает активные выдачи книги с указанным статусом
     *
     * @param book книга
     * @param status статус выдачи
     * @return количество выдач
     */
    Long countByBookAndStatus(Book book, String status);

    /**
     * Возвращает выдачи пользователя с указанным статусом
     *
     * @param user пользователь
     * @param status статус выдачи
     * @return список выдач
     */
    List<Loan> findByUserAndStatus(User user, String status);

    /**
     * Находит выдачу по id и владельцу
     *
     * @param id идентификатор выдачи
     * @param user пользователь
     * @return выдача, если найдена
     */
    Optional<Loan> findByIdAndUser(Long id, User user);
}
