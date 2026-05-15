package org.dzhabarov.naujavaproject.controller;

import org.dzhabarov.naujavaproject.entity.Loan;
import org.dzhabarov.naujavaproject.entity.Reservation;
import org.dzhabarov.naujavaproject.entity.User;
import org.dzhabarov.naujavaproject.exception.BusinessException;
import org.dzhabarov.naujavaproject.service.AppTimeService;
import org.dzhabarov.naujavaproject.service.LibraryService;
import org.dzhabarov.naujavaproject.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Веб-интерфейс резервирования, выдачи и возврата книг
 */
@Controller
@RequestMapping("/library")
public class LibraryViewController {

    private final LibraryService libraryService;
    private final UserService userService;
    private final AppTimeService appTimeService;

    /**
     * @param libraryService сервис операций библиотеки
     * @param userService сервис пользователей
     * @param appTimeService время в зоне приложения
     */
    public LibraryViewController(LibraryService libraryService,
                                 UserService userService,
                                 AppTimeService appTimeService) {
        this.libraryService = libraryService;
        this.userService = userService;
        this.appTimeService = appTimeService;
    }

    /** Страница «Моя библиотека»: активные резервы и выдачи */
    @GetMapping("/my")
    public String myLibrary(Principal principal, Model model) {
        User user = getCurrentUser(principal);
        libraryService.expireOutdatedReservations();

        List<ReservationView> reservations = libraryService.getActiveReservations(user).stream()
                .map(item -> new ReservationView(
                        item.getId(),
                        item.getBook().getId(),
                        item.getBook().getTitle(),
                        item.getReservationDate(),
                        item.getStatus(),
                        libraryService.resolvePickupState(item),
                        libraryService.getPickupDeadline(item)))
                .toList();

        List<LoanView> loans = libraryService.getActiveLoans(user).stream()
                .map(item -> new LoanView(
                        item.getId(),
                        item.getBook().getId(),
                        item.getBook().getTitle(),
                        item.getIssueDate(),
                        item.getDueDate(),
                        item.getStatus()))
                .toList();

        model.addAttribute("reservations", reservations);
        model.addAttribute("loans", loans);
        model.addAttribute("serverNow", appTimeService.formatDisplay(appTimeService.now()));
        model.addAttribute("timeZoneLabel", appTimeService.getZoneDisplayName());
        return "my-library";
    }

    /** Резервирует книгу на указанные дату и время */
    @PostMapping("/books/{bookId}/reserve")
    public String reserve(@PathVariable Long bookId,
                          @RequestParam String reservationDateTime,
                          @RequestParam int timezoneOffsetMinutes,
                          Principal principal,
                          RedirectAttributes redirectAttributes) {
        try {
            LocalDateTime reservationTime = appTimeService.parseReservationFromBrowser(
                    reservationDateTime, timezoneOffsetMinutes);
            libraryService.reserveBook(
                    getCurrentUser(principal),
                    bookId,
                    reservationTime
            );
            redirectAttributes.addFlashAttribute("successMessage", "Книга успешно зарезервирована");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Некорректная дата/время резервирования");
        }
        return "redirect:/books";
    }

    /** Выдаёт книгу по резерву или из свободного фонда */
    @PostMapping("/books/{bookId}/take")
    public String takeBook(@PathVariable Long bookId,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        try {
            libraryService.takeBook(getCurrentUser(principal), bookId);
            redirectAttributes.addFlashAttribute("successMessage", "Книга успешно выдана");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/books";
    }

    /** Отменяет активное резервирование */
    @PostMapping("/reservations/{reservationId}/cancel")
    public String cancelReservation(@PathVariable Long reservationId,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        try {
            libraryService.cancelReservation(getCurrentUser(principal), reservationId);
            redirectAttributes.addFlashAttribute("successMessage", "Резервирование отменено");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/library/my";
    }

    /** Возвращает книгу в библиотеку */
    @PostMapping("/loans/{loanId}/return")
    public String returnBook(@PathVariable Long loanId,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        try {
            libraryService.returnBook(getCurrentUser(principal), loanId);
            redirectAttributes.addFlashAttribute("successMessage", "Книга успешно возвращена");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/library/my";
    }

    /** Возвращает текущего авторизованного пользователя */
    private User getCurrentUser(Principal principal) {
        return userService.findByName(principal.getName());
    }

    /** DTO резервирования для шаблона my-library */
    public record ReservationView(Long id,
                                  Long bookId,
                                  String bookTitle,
                                  LocalDateTime reservationDate,
                                  String status,
                                  String pickupState,
                                  LocalDateTime pickupDeadline) {
    }

    /** DTO выдачи для шаблона my-library */
    public record LoanView(Long id, Long bookId, String bookTitle, LocalDate issueDate, LocalDate dueDate, String status) {
    }
}
