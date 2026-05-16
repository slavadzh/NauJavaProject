package org.dzhabarov.naujavaproject.controller;

import org.dzhabarov.naujavaproject.dto.BookDTO;
import org.dzhabarov.naujavaproject.entity.Reservation;
import org.dzhabarov.naujavaproject.entity.User;
import org.dzhabarov.naujavaproject.service.AppTimeService;
import org.dzhabarov.naujavaproject.service.BookService;
import org.dzhabarov.naujavaproject.service.LibraryService;
import org.dzhabarov.naujavaproject.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Веб-страница каталога книг: поиск, резерв, выдача
 */
@Controller
public class BookViewController {

    private final BookService bookService;
    private final UserService userService;
    private final LibraryService libraryService;
    private final AppTimeService appTimeService;

    /**
     * @param bookService сервис каталога
     * @param userService сервис пользователей
     * @param libraryService сервис резервов и выдач
     * @param appTimeService время в зоне приложения
     */
    public BookViewController(BookService bookService,
                              UserService userService,
                              LibraryService libraryService,
                              AppTimeService appTimeService) {
        this.bookService = bookService;
        this.userService = userService;
        this.libraryService = libraryService;
        this.appTimeService = appTimeService;
    }

    /** Отображает каталог с фильтрами и кнопками действий */
    @GetMapping("/books")
    public String getBooks(@RequestParam(required = false) String title,
                           @RequestParam(required = false) String author,
                           @RequestParam(required = false) String genre,
                           @RequestParam(required = false) Integer year,
                           Principal principal,
                           Model model) {
        List<BookDTO> books = (title == null && author == null && genre == null && year == null)
                ? bookService.findAll()
                : bookService.search(title, author, genre, year);
        model.addAttribute(
                "books",
                books
        );
        model.addAttribute("title", title);
        model.addAttribute("author", author);
        model.addAttribute("genre", genre);
        model.addAttribute("year", year);
        User user = userService.findByName(principal.getName());
        libraryService.expireOutdatedReservations();

        Set<Long> pickupReadyBookIds = new HashSet<>();
        Set<Long> waitingReservationBookIds = new HashSet<>();
        Set<Long> pickupMissedBookIds = new HashSet<>();
        Set<Long> reservedBookIds = new HashSet<>();
        for (Reservation reservation : libraryService.getActiveReservations(user)) {
            Long bookId = reservation.getBook().getId();
            reservedBookIds.add(bookId);
            switch (libraryService.resolvePickupState(reservation)) {
                case "READY" -> pickupReadyBookIds.add(bookId);
                case "WAITING" -> waitingReservationBookIds.add(bookId);
                case "MISSED" -> pickupMissedBookIds.add(bookId);
                default -> { }
            }
        }

        model.addAttribute("isAdmin", userService.isAdmin(principal.getName()));
        model.addAttribute("pickupReadyBookIds", pickupReadyBookIds);
        model.addAttribute("waitingReservationBookIds", waitingReservationBookIds);
        model.addAttribute("pickupMissedBookIds", pickupMissedBookIds);
        model.addAttribute("reservedBookIds", reservedBookIds);
        model.addAttribute("minReservationDateTime", appTimeService.minReservationDateTimeForInput());
        model.addAttribute("serverNow", appTimeService.formatDisplay(appTimeService.now()));
        model.addAttribute("timeZoneLabel", appTimeService.getZoneDisplayName());
        return "books";
    }
}