package org.dzhabarov.naujavaproject.service;

import lombok.extern.slf4j.Slf4j;
import org.dzhabarov.naujavaproject.dto.BookDTO;
import org.dzhabarov.naujavaproject.entity.Book;
import org.dzhabarov.naujavaproject.entity.Loan;
import org.dzhabarov.naujavaproject.entity.Reservation;
import org.dzhabarov.naujavaproject.exception.BusinessException;
import org.dzhabarov.naujavaproject.mapper.BookMapper;
import org.dzhabarov.naujavaproject.repository.BookRepository;
import org.dzhabarov.naujavaproject.repository.LoanRepository;
import org.dzhabarov.naujavaproject.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.dzhabarov.naujavaproject.config.TimeConfig.now;

/**
 * Бизнес-логика каталога книг: поиск, CRUD, расчёт доступных экземпляров
 */
@Slf4j
@Service
public class BookService {

    /** Окно получения по резерву (минуты), для расчёта доступности */
    private static final int RESERVATION_PICKUP_WINDOW_MINUTES = 30;

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    /**
     * @param bookRepository репозиторий книг
     * @param bookMapper маппер книг в DTO
     * @param loanRepository репозиторий выдач
     * @param reservationRepository репозиторий резервов
     * @param clock часы в зоне {@code app.time-zone}
     */
    public BookService(BookRepository bookRepository,
                       BookMapper bookMapper,
                       LoanRepository loanRepository,
                       ReservationRepository reservationRepository,
                       Clock clock) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.loanRepository = loanRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    /** Поиск книг по части названия */
    public List<BookDTO> findByTitleContaining(String title) {
        return bookRepository.findByTitleContainingCustom(title).stream()
                .map(this::toBookDTOWithAvailability).collect(Collectors.toList());
    }

    /** Поиск книг по имени автора */
    public List<BookDTO> findByAuthor(String name) {
        return bookRepository.findByAuthorNameCustom(name).stream()
                .map(this::toBookDTOWithAvailability).collect(Collectors.toList());
    }

    /** Возвращает весь каталог с количеством доступных экземпляров */
    public List<BookDTO> findAll() {
        return bookRepository.findAll().stream().map(this::toBookDTOWithAvailability).collect(Collectors.toList());
    }

    /** Возвращает сущности книг с авторами (для админ-панели) */
    @Transactional(readOnly = true)
    public List<Book> findAllEntities() {
        return bookRepository.findAllWithAuthors();
    }

    /**
     * Комбинированный поиск по названию, автору, жанру и году
     * Пустые параметры не учитываются
     */
    public List<BookDTO> search(String title, String author, String genre, Integer year) {
        return bookRepository.findAll().stream()
                .filter(book -> matches(book.getTitle(), title))
                .filter(book -> matches(book.getGenre(), genre))
                .filter(book -> year == null || year.equals(book.getPublicationYear()))
                .filter(book -> author == null || author.isBlank() || (book.getAuthors() != null && book.getAuthors().stream()
                        .anyMatch(item -> matches(item.getName(), author))))
                .map(this::toBookDTOWithAvailability)
                .collect(Collectors.toList());
    }

    /** Создаёт новую книгу в каталоге */
    @Transactional
    public Book create(Book book) {
        if (book.getAuthors() == null) {
            book.setAuthors(new HashSet<>());
        }
        validateCopies(book);
        Book saved = bookRepository.save(book);
        log.info("Book created: id={}, title={}", saved.getId(), saved.getTitle());
        return saved;
    }

    /** Обновляет данные книги */
    @Transactional
    public Book update(Long id, Book book) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Книга не найдена"));
        existing.setTitle(book.getTitle());
        existing.setGenre(book.getGenre());
        existing.setPublicationYear(book.getPublicationYear());
        existing.setIsbn(book.getIsbn());
        existing.setAuthors(book.getAuthors() == null ? new HashSet<>() : book.getAuthors());
        existing.setTotalCopies(book.getTotalCopies());
        validateCopies(existing);
        return bookRepository.save(existing);
    }

    /** Удаляет книгу из каталога */
    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new BusinessException("Книга не найдена");
        }
        bookRepository.deleteById(id);
    }

    /** Проверяет вхождение подстроки без учёта регистра */
    private boolean matches(String value, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        return value != null && value.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT));
    }

    /** Проверяет, что число экземпляров неотрицательное */
    private void validateCopies(Book book) {
        if (book.getTotalCopies() == null || book.getTotalCopies() < 0) {
            throw new BusinessException("Количество экземпляров не может быть отрицательным");
        }
    }

    /** Преобразует книгу в DTO и рассчитывает поле «доступно сейчас» */
    private BookDTO toBookDTOWithAvailability(Book book) {
        BookDTO dto = bookMapper.toBookDTO(book);
        int total = book.getTotalCopies() == null ? 0 : book.getTotalCopies();
        long activeLoans = loanRepository.countByBookAndStatus(book, Loan.STATUS_ACTIVE);
        LocalDateTime now = now(clock);
        long activeReservations = reservationRepository.findByBookAndStatus(book, Reservation.STATUS_ACTIVE).stream()
                .filter(item -> !now.isBefore(item.getReservationDate()))
                .filter(item -> !now.isAfter(item.getReservationDate().plusMinutes(RESERVATION_PICKUP_WINDOW_MINUTES)))
                .count();
        int available = (int) Math.max(0, total - activeLoans - activeReservations);
        dto.setAvailableCopies(available);
        return dto;
    }
}
