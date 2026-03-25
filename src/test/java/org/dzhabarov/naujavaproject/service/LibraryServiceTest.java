package org.dzhabarov.naujavaproject.service;

import org.dzhabarov.naujavaproject.entity.Book;
import org.dzhabarov.naujavaproject.entity.Loan;
import org.dzhabarov.naujavaproject.entity.Reservation;
import org.dzhabarov.naujavaproject.entity.User;
import org.dzhabarov.naujavaproject.repository.BookRepository;
import org.dzhabarov.naujavaproject.repository.LoanRepository;
import org.dzhabarov.naujavaproject.repository.ReservationRepository;
import org.dzhabarov.naujavaproject.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LibraryServiceTest {

    @Autowired
    private LibraryService libraryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void testTakeBook() {

        User user = new User();
        user.setName("Test");
        userRepository.save(user);

        Book book = new Book();
        book.setTitle("Test Book");
        bookRepository.save(book);

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservationRepository.save(reservation);

        libraryService.takeBook(user, book);

        List<Loan> loans = loanRepository.findAll();
        assertFalse(loans.isEmpty());

        List<Reservation> reservations =
                reservationRepository.findByUserAndBook(user, book);

        assertTrue(reservations.isEmpty());
    }
}