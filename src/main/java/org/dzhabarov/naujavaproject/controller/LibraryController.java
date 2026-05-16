package org.dzhabarov.naujavaproject.controller;

import org.dzhabarov.naujavaproject.entity.Loan;
import org.dzhabarov.naujavaproject.entity.Reservation;
import org.dzhabarov.naujavaproject.entity.User;
import org.dzhabarov.naujavaproject.service.LibraryService;
import org.dzhabarov.naujavaproject.service.UserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/**
 * REST API операций библиотеки: резерв, выдача, возврат
 */
@RestController
@RequestMapping("/api/library")
public class LibraryController {

    private final LibraryService libraryService;
    private final UserService userService;

    /**
     * @param libraryService сервис операций библиотеки
     * @param userService сервис пользователей
     */
    public LibraryController(LibraryService libraryService, UserService userService) {
        this.libraryService = libraryService;
        this.userService = userService;
    }

    /** Резервирует книгу на текущее время */
    @PostMapping("/books/{bookId}/reserve")
    public Reservation reserve(@PathVariable Long bookId, Principal principal) {
        return libraryService.reserveBook(getCurrentUser(principal), bookId);
    }

    /** Отменяет резервирование */
    @DeleteMapping("/reservations/{reservationId}")
    public void cancelReservation(@PathVariable Long reservationId, Principal principal) {
        libraryService.cancelReservation(getCurrentUser(principal), reservationId);
    }

    /** Выдаёт книгу на руки */
    @PostMapping("/books/{bookId}/take")
    public Loan takeBook(@PathVariable Long bookId, Principal principal) {
        return libraryService.takeBook(getCurrentUser(principal), bookId);
    }

    /** Возвращает книгу в библиотеку */
    @PostMapping("/loans/{loanId}/return")
    public void returnBook(@PathVariable Long loanId, Principal principal) {
        libraryService.returnBook(getCurrentUser(principal), loanId);
    }

    /** Возвращает активные резервирования текущего пользователя */
    @GetMapping("/me/reservations")
    public List<Reservation> myReservations(Principal principal) {
        return libraryService.getActiveReservations(getCurrentUser(principal));
    }

    /** Возвращает активные выдачи текущего пользователя */
    @GetMapping("/me/loans")
    public List<Loan> myLoans(Principal principal) {
        return libraryService.getActiveLoans(getCurrentUser(principal));
    }

    /** Возвращает текущего авторизованного пользователя */
    private User getCurrentUser(Principal principal) {
        return userService.findByName(principal.getName());
    }
}
