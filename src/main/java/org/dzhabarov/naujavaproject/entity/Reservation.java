package org.dzhabarov.naujavaproject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Резервирование книги пользователем на указанное время
 */
@Entity
@Getter
@Setter
@Table(name = "reservation")
public class Reservation {

    /** Активный резерв — ожидает получения книги */
    public static final String STATUS_ACTIVE = "ACTIVE";
    /** Резерв отменён пользователем */
    public static final String STATUS_CANCELLED = "CANCELLED";
    /** Книга выдана по резерву */
    public static final String STATUS_FULFILLED = "FULFILLED";
    /** Истёк срок получения (30 минут после времени резерва) */
    public static final String STATUS_EXPIRED = "EXPIRED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Дата и время, на которое забронирована книга */
    private LocalDateTime reservationDate;
    /** Текущий статус резервирования */
    private String status;
    /** Дополнительный комментарий */
    private String comment;

    @ManyToOne
    private User user;

    @ManyToOne
    private Book book;
}
