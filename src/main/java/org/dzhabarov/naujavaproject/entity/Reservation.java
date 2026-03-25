package org.dzhabarov.naujavaproject.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate reservationDate;
    private String status;
    private String comment;

    @ManyToOne
    private User user;

    @ManyToOne
    private Book book;
}
