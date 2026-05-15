package org.dzhabarov.naujavaproject.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * Книга в каталоге библиотеки
 */
@Entity
@Getter
@Setter
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Integer publicationYear;
    private String genre;
    private String isbn;
    /** Общее количество экземпляров в фонде */
    private Integer totalCopies;

    @ManyToMany
    @JoinTable(
            name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors;

    @OneToMany(mappedBy = "book")
    @JsonIgnore
    private List<Loan> loans;

    @OneToMany(mappedBy = "book")
    @JsonIgnore
    private List<Reservation> reservations;
}
