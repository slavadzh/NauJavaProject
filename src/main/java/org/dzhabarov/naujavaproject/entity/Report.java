package org.dzhabarov.naujavaproject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Сущность отчёта: статус и опциональное содержимое
 */
@Entity
@Setter
@Getter
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;

    private String content;
}
