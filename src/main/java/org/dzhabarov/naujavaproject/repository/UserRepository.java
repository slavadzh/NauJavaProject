package org.dzhabarov.naujavaproject.repository;

import org.dzhabarov.naujavaproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с пользователями в БД
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по логину
     *
     * @param name логин
     * @return пользователь, если найден
     */
    Optional<User> findByName(String name);

    /**
     * Находит пользователей по логину и email
     *
     * @param name логин
     * @param email email
     * @return список пользователей
     */
    List<User> findByNameAndEmail(String name, String email);

    /**
     * Находит пользователей по email (JPQL)
     *
     * @param email email
     * @return список пользователей
     */
    @Query("FROM User u WHERE u.email = :email")
    List<User> findByEmailCustom(String email);

    /**
     * Проверяет существование пользователя с таким логином
     *
     * @param name логин
     * @return true, если логин занят
     */
    boolean existsByName(String name);

    /**
     * Проверяет существование пользователя с таким email
     *
     * @param email email
     * @return true, если email занят
     */
    boolean existsByEmail(String email);
}
