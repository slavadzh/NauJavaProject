package org.dzhabarov.naujavaproject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Пользователь системы (читатель или администратор)
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Логин для входа */
    private String name;
    private String email;
    private String phone;
    /** Хэш пароля (BCrypt) */
    private String password;
    private LocalDate registrationDate;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Loan> loans;

    @OneToMany(mappedBy = "user")
    private List<Reservation> reservations;
}
