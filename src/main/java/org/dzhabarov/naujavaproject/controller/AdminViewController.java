package org.dzhabarov.naujavaproject.controller;

import org.dzhabarov.naujavaproject.entity.Author;
import org.dzhabarov.naujavaproject.entity.Book;
import org.dzhabarov.naujavaproject.entity.Loan;
import org.dzhabarov.naujavaproject.entity.Reservation;
import org.dzhabarov.naujavaproject.exception.BusinessException;
import org.dzhabarov.naujavaproject.service.AuthorService;
import org.dzhabarov.naujavaproject.service.BookService;
import org.dzhabarov.naujavaproject.service.LibraryService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Панель администратора: книги, авторы, все резервы и выдачи
 */
@Controller
@RequestMapping("/admin")
public class AdminViewController {

    private final BookService bookService;
    private final AuthorService authorService;
    private final LibraryService libraryService;

    /**
     * @param bookService сервис каталога
     * @param authorService сервис авторов
     * @param libraryService сервис резервов и выдач
     */
    public AdminViewController(BookService bookService,
                             AuthorService authorService,
                             LibraryService libraryService) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.libraryService = libraryService;
    }

    /** Главная страница админ-панели */
    @GetMapping
    public String adminPage(Model model) {
        List<Book> books = bookService.findAllEntities();
        model.addAttribute("books", books);
        model.addAttribute("bookAuthorIds", buildBookAuthorIds(books));
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("reservations", buildReservationViews(libraryService.getAllReservations()));
        model.addAttribute("loans", buildLoanViews(libraryService.getAllLoans()));
        model.addAttribute("maxBirthDate", LocalDate.now());
        return "admin";
    }

    /** Добавляет новую книгу в каталог */
    @PostMapping("/books/create")
    public String createBook(@RequestParam String title,
                             @RequestParam Integer publicationYear,
                             @RequestParam String genre,
                             @RequestParam String isbn,
                             @RequestParam Integer totalCopies,
                             @RequestParam(required = false) Set<Long> authorIds,
                             RedirectAttributes redirectAttributes) {
        try {
            Book book = new Book();
            book.setTitle(title);
            book.setPublicationYear(publicationYear);
            book.setGenre(genre);
            book.setIsbn(isbn);
            book.setTotalCopies(totalCopies);
            book.setAuthors(authorService.resolveAuthors(authorIds));
            bookService.create(book);
            redirectAttributes.addFlashAttribute("successMessage", "Книга добавлена");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Книга с такими данными уже есть (проверьте ISBN)");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось добавить книгу");
        }
        return "redirect:/admin";
    }

    /** Обновляет данные книги */
    @PostMapping("/books/{id}/update")
    public String updateBook(@PathVariable Long id,
                             @RequestParam String title,
                             @RequestParam Integer publicationYear,
                             @RequestParam String genre,
                             @RequestParam String isbn,
                             @RequestParam Integer totalCopies,
                             @RequestParam(required = false) Set<Long> authorIds,
                             RedirectAttributes redirectAttributes) {
        try {
            Book book = new Book();
            book.setTitle(title);
            book.setPublicationYear(publicationYear);
            book.setGenre(genre);
            book.setIsbn(isbn);
            book.setTotalCopies(totalCopies);
            book.setAuthors(authorService.resolveAuthors(authorIds));
            bookService.update(id, book);
            redirectAttributes.addFlashAttribute("successMessage", "Книга обновлена");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось обновить книгу");
        }
        return "redirect:/admin";
    }

    /** Удаляет книгу из каталога */
    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Книга удалена");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось удалить книгу");
        }
        return "redirect:/admin";
    }

    /** Добавляет нового автора */
    @PostMapping("/authors/create")
    public String createAuthor(@RequestParam String name,
                               @RequestParam(required = false) String country,
                               @RequestParam(required = false) String birthDate,
                               RedirectAttributes redirectAttributes) {
        try {
            authorService.create(name, country, parseBirthDate(birthDate));
            redirectAttributes.addFlashAttribute("successMessage", "Автор добавлен");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось добавить автора");
        }
        return "redirect:/admin";
    }

    /** Обновляет данные автора */
    @PostMapping("/authors/{id}/update")
    public String updateAuthor(@PathVariable Long id,
                               @RequestParam String name,
                               @RequestParam(required = false) String country,
                               @RequestParam(required = false) String birthDate,
                               RedirectAttributes redirectAttributes) {
        try {
            authorService.update(id, name, country, parseBirthDate(birthDate));
            redirectAttributes.addFlashAttribute("successMessage", "Автор обновлен");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось обновить автора");
        }
        return "redirect:/admin";
    }

    /** Удаляет автора */
    @PostMapping("/authors/{id}/delete")
    public String deleteAuthor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            authorService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Автор удален");
        } catch (BusinessException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось удалить автора");
        }
        return "redirect:/admin";
    }

    /** ID авторов по каждой книге (для чекбоксов в admin.html) */
    private Map<Long, Set<Long>> buildBookAuthorIds(List<Book> books) {
        return books.stream().collect(Collectors.toMap(
                Book::getId,
                book -> book.getAuthors() == null
                        ? Set.of()
                        : book.getAuthors().stream().map(Author::getId).collect(Collectors.toSet())
        ));
    }

    /** Парсит дату рождения из строки формы */
    private LocalDate parseBirthDate(String birthDate) {
        if (birthDate == null || birthDate.isBlank()) {
            return null;
        }
        return LocalDate.parse(birthDate);
    }

    /** Преобразует резервы в представление для таблицы админки */
    private List<ReservationView> buildReservationViews(List<Reservation> reservations) {
        return reservations.stream()
                .map(item -> new ReservationView(
                        item.getId(),
                        item.getBook() == null ? "-" : item.getBook().getTitle(),
                        item.getUser() == null ? "-" : item.getUser().getName(),
                        item.getReservationDate(),
                        item.getStatus()))
                .toList();
    }

    /** Преобразует выдачи в представление для таблицы админки */
    private List<LoanView> buildLoanViews(List<Loan> loans) {
        return loans.stream()
                .map(item -> new LoanView(
                        item.getId(),
                        item.getBook() == null ? "-" : item.getBook().getTitle(),
                        item.getUser() == null ? "-" : item.getUser().getName(),
                        item.getIssueDate(),
                        item.getDueDate(),
                        item.getReturnDate(),
                        item.getStatus()))
                .toList();
    }

    /** DTO резервирования для шаблона admin */
    public record ReservationView(Long id, String bookTitle, String username, LocalDateTime reservationDate, String status) {
    }

    /** DTO выдачи для шаблона admin */
    public record LoanView(Long id, String bookTitle, String username, LocalDate issueDate, LocalDate dueDate, LocalDate returnDate,
                           String status) {
    }
}
