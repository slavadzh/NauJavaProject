package org.dzhabarov.naujavaproject.repository;

import org.dzhabarov.naujavaproject.entity.Book;
import org.dzhabarov.naujavaproject.entity.Reservation;
import org.dzhabarov.naujavaproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с резервированиями в БД
 */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /**
     * Возвращает резервирования пользователя по конкретной книге
     *
     * @param user пользователь
     * @param book книга
     * @return список резервирований
     */
    List<Reservation> findByUserAndBook(User user, Book book);

    /**
     * Возвращает резервирования пользователя с указанным статусом
     *
     * @param user пользователь
     * @param status статус резерва
     * @return список резервирований
     */
    List<Reservation> findByUserAndStatus(User user, String status);

    /**
     * Считает активные резервы книги с указанным статусом
     *
     * @param book книга
     * @param status статус резерва
     * @return количество резервов
     */
    Long countByBookAndStatus(Book book, String status);

    /**
     * Находит резервирование по id и владельцу
     *
     * @param id идентификатор резерва
     * @param user пользователь
     * @return резервирование, если найдено
     */
    Optional<Reservation> findByIdAndUser(Long id, User user);

    /**
     * Находит первое резервирование пользователя по книге и статусу
     *
     * @param user пользователь
     * @param book книга
     * @param status статус резерва
     * @return резервирование, если найдено
     */
    Optional<Reservation> findFirstByUserAndBookAndStatus(User user, Book book, String status);

    /**
     * Проверяет наличие активного резерва пользователя на книгу
     *
     * @param user пользователь
     * @param book книга
     * @param status статус резерва
     * @return true, если резерв существует
     */
    boolean existsByUserAndBookAndStatus(User user, Book book, String status);

    /**
     * Возвращает резервирования книги с указанным статусом
     *
     * @param book книга
     * @param status статус резерва
     * @return список резервирований
     */
    List<Reservation> findByBookAndStatus(Book book, String status);

    /**
     * Возвращает все резервирования с указанным статусом
     *
     * @param status статус резерва
     * @return список резервирований
     */
    List<Reservation> findByStatus(String status);
}
