package org.dzhabarov.naujavaproject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Выдача книги пользователю на руки
 */
@Entity
@Getter
@Setter
@Table(name = "loan")
public class Loan {

    /** Книга на руках у читателя */
    public static final String STATUS_ACTIVE = "ACTIVE";
    /** Книга возвращена в библиотеку */
    public static final String STATUS_RETURNED = "RETURNED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Дата выдачи */
    private LocalDate issueDate;
    /** Плановая дата возврата (выдача + 2 недели) */
    private LocalDate dueDate;
    /** Фактическая дата возврата */
    private LocalDate returnDate;
    /** Текущий статус выдачи */
    private String status;

    @ManyToOne
    private User user;

    @ManyToOne
    private Book book;
}
