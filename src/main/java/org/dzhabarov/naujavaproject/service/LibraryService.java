package org.dzhabarov.naujavaproject.service;

import org.dzhabarov.naujavaproject.entity.Book;
import org.dzhabarov.naujavaproject.entity.Loan;
import org.dzhabarov.naujavaproject.entity.Reservation;
import org.dzhabarov.naujavaproject.entity.User;
import org.dzhabarov.naujavaproject.repository.LoanRepository;
import org.dzhabarov.naujavaproject.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class LibraryService {

    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;

    public LibraryService(LoanRepository loanRepository,
                          ReservationRepository reservationRepository) {
        this.loanRepository = loanRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public void takeBook(User user, Book book) {

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setBook(book);
        loan.setIssueDate(LocalDate.now());

        loanRepository.save(loan);

        List<Reservation> reservations =
                reservationRepository.findByUserAndBook(user, book);

        reservationRepository.deleteAll(reservations);
    }
}
