package org.dzhabarov.naujavaproject.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate issueDate;
    private LocalDate returnDate;
    private String status;

    @ManyToOne
    private User user;

    @ManyToOne
    private Book book;
}