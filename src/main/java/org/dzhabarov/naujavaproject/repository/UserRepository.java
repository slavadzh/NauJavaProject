package org.dzhabarov.naujavaproject.repository;

import org.dzhabarov.naujavaproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByNameAndEmail(String name, String email);

    @Query("FROM User u WHERE u.email = :email")
    List<User> findByEmailCustom(String email);
}