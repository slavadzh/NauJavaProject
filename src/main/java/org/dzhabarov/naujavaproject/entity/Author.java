package org.dzhabarov.naujavaproject.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

/**
 * Автор книги
 */
@Entity
@Getter
@Setter
@Table(name = "author")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private LocalDate birthDate;
    private String country;

    @ManyToMany(mappedBy = "authors")
    @JsonIgnore
    private Set<Book> books;
}
