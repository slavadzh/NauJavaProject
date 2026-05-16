package org.dzhabarov.naujavaproject.service;

import lombok.extern.slf4j.Slf4j;
import org.dzhabarov.naujavaproject.entity.Book;
import org.dzhabarov.naujavaproject.entity.Loan;
import org.dzhabarov.naujavaproject.entity.Reservation;
import org.dzhabarov.naujavaproject.entity.User;
import org.dzhabarov.naujavaproject.exception.BusinessException;
import org.dzhabarov.naujavaproject.repository.BookRepository;
import org.dzhabarov.naujavaproject.repository.LoanRepository;
import org.dzhabarov.naujavaproject.repository.ReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.dzhabarov.naujavaproject.config.TimeConfig.now;

/**
 * Бизнес-логика резервирования, выдачи и возврата книг
 */
@Slf4j
@Service
public class LibraryService {

    /** Срок получения книги по резерву после наступления времени (в минутах) */
    private static final int RESERVATION_PICKUP_WINDOW_MINUTES = 30;

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final ReservationRepository reservationRepository;
    private final Clock clock;

    /**
     * @param bookRepository репозиторий книг
     * @param loanRepository репозиторий выдач
     * @param reservationRepository репозиторий резервов
     * @param clock часы в зоне {@code app.time-zone}
     */
    public LibraryService(BookRepository bookRepository,
                          LoanRepository loanRepository,
                          ReservationRepository reservationRepository,
                          Clock clock) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.reservationRepository = reservationRepository;
        this.clock = clock;
    }

    /**
     * Резервирует книгу на текущее время
     */
    @Transactional
    public Reservation reserveBook(User user, Long bookId) {
        return reserveBook(user, bookId, now(clock));
    }

    /**
     * Резервирует книгу на указанные дату и время
     *
     * @throws BusinessException если нет экземпляров или резерв уже существует
     */
    @Transactional
    public Reservation reserveBook(User user, Long bookId, LocalDateTime reservationDateTime) {
        Book book = getBookById(bookId);
        validateReservationDateTime(reservationDateTime);
        validateBookAvailability(book, false, reservationDateTime);
        if (reservationRepository.existsByUserAndBookAndStatus(user, book, Reservation.STATUS_ACTIVE)) {
            throw new BusinessException("У вас уже есть активное резервирование этой книги");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setReservationDate(reservationDateTime);
        reservation.setStatus(Reservation.STATUS_ACTIVE);
        return reservationRepository.save(reservation);
    }

    /**
     * Отменяет активное резервирование пользователя
     */
    @Transactional
    public void cancelReservation(User user, Long reservationId) {
        Reservation reservation = reservationRepository.findByIdAndUser(reservationId, user)
                .orElseThrow(() -> new BusinessException("Резервирование не найдено"));
        if (!Reservation.STATUS_ACTIVE.equals(reservation.getStatus())) {
            throw new BusinessException("Можно отменять только активное резервирование");
        }
        reservation.setStatus(Reservation.STATUS_CANCELLED);
        reservationRepository.save(reservation);
    }

    /**
     * Выдаёт книгу на руки (на 2 недели). Учитывает активный резерв пользователя
     */
    @Transactional
    public Loan takeBook(User user, Long bookId) {
        Book book = getBookById(bookId);
        LocalDateTime now = now(clock);
        Reservation ownReservation = reservationRepository
                .findFirstByUserAndBookAndStatus(user, book, Reservation.STATUS_ACTIVE)
                .orElse(null);

        boolean canTakeByReservation = false;
        if (ownReservation != null) {
            if (isReservationExpired(ownReservation, now)) {
                ownReservation.setStatus(Reservation.STATUS_EXPIRED);
                reservationRepository.save(ownReservation);
                throw new BusinessException("Время получения по резерву истекло");
            }
            if (now.isBefore(ownReservation.getReservationDate())) {
                throw new BusinessException("Забрать книгу можно только после наступления времени резерва");
            }
            canTakeByReservation = true;
        }
        validateBookAvailability(book, canTakeByReservation, now);

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setBook(book);
        loan.setIssueDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusWeeks(2));
        loan.setStatus(Loan.STATUS_ACTIVE);
        Loan savedLoan = loanRepository.save(loan);

        List<Reservation> reservations = reservationRepository.findByUserAndBook(user, book);
        reservations.stream()
                .filter(reservation -> Reservation.STATUS_ACTIVE.equals(reservation.getStatus()))
                .forEach(reservation -> reservation.setStatus(Reservation.STATUS_FULFILLED));
        reservationRepository.saveAll(reservations);
        return savedLoan;
    }

    /**
     * Возвращает книгу в библиотеку (досрочно или в срок)
     */
    @Transactional
    public void returnBook(User user, Long loanId) {
        Loan loan = loanRepository.findByIdAndUser(loanId, user)
                .orElseThrow(() -> new BusinessException("Выдача не найдена"));
        if (!Loan.STATUS_ACTIVE.equals(loan.getStatus())) {
            throw new BusinessException("Книга уже возвращена");
        }
        loan.setStatus(Loan.STATUS_RETURNED);
        loan.setReturnDate(LocalDate.now());
        loanRepository.save(loan);
    }

    /** Возвращает активные резервирования пользователя */
    public List<Reservation> getActiveReservations(User user) {
        return reservationRepository.findByUserAndStatus(user, Reservation.STATUS_ACTIVE);
    }

    /** Возвращает активные выдачи пользователя */
    public List<Loan> getActiveLoans(User user) {
        return loanRepository.findByUserAndStatus(user, Loan.STATUS_ACTIVE);
    }

    /** Возвращает все резервирования (для администратора) */
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    /** Возвращает все выдачи (для администратора) */
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    /** Проверяет, открыто ли окно получения по резерву (30 минут) */
    public boolean isPickupWindowOpen(Reservation reservation) {
        LocalDateTime now = now(clock);
        return !now.isBefore(reservation.getReservationDate())
                && !isReservationExpired(reservation, now);
    }

    /** Проверяет, что время резерва ещё не наступило */
    public boolean isBeforePickupWindow(Reservation reservation) {
        return now(clock).isBefore(reservation.getReservationDate());
    }

    /** Окно получения прошло, но резерв ещё ACTIVE (до планировщика) */
    public boolean isPickupWindowMissed(Reservation reservation) {
        LocalDateTime now = now(clock);
        return !now.isBefore(reservation.getReservationDate()) && isReservationExpired(reservation, now);
    }

    /** Состояние кнопки в UI: WAITING, READY, MISSED */
    public String resolvePickupState(Reservation reservation) {
        if (isPickupWindowOpen(reservation)) {
            return "READY";
        }
        if (isBeforePickupWindow(reservation)) {
            return "WAITING";
        }
        return "MISSED";
    }

    /** Возвращает крайний срок получения книги по резерву */
    public LocalDateTime getPickupDeadline(Reservation reservation) {
        return reservation.getReservationDate().plusMinutes(RESERVATION_PICKUP_WINDOW_MINUTES);
    }

    /**
     * Планировщик: переводит просроченные резервы в статус EXPIRED
     * Запускается каждую минуту
     */
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expireOutdatedReservations() {
        LocalDateTime now = now(clock);
        List<Reservation> activeReservations = reservationRepository.findByStatus(Reservation.STATUS_ACTIVE);
        List<Reservation> expired = activeReservations.stream()
                .filter(item -> isReservationExpired(item, now))
                .peek(item -> item.setStatus(Reservation.STATUS_EXPIRED))
                .toList();
        if (!expired.isEmpty()) {
            reservationRepository.saveAll(expired);
            log.info("Expired reservations: {}", expired.size());
        }
    }

    /** Загружает книгу по id или бросает BusinessException */
    private Book getBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException("Книга не найдена"));
    }

    /** Проверяет наличие свободного экземпляра с учётом выдач и резервов */
    private void validateBookAvailability(Book book, boolean hasOwnReservation, LocalDateTime now) {
        int copies = book.getTotalCopies() == null ? 0 : book.getTotalCopies();
        long activeLoans = loanRepository.countByBookAndStatus(book, Loan.STATUS_ACTIVE);
        long activeReservations = countBlockingReservations(book, now);
        if (hasOwnReservation && activeReservations > 0) {
            activeReservations -= 1;
        }
        long available = copies - activeLoans - activeReservations;
        if (available <= 0) {
            throw new BusinessException("Нет доступных экземпляров");
        }
    }

    /** Проверяет корректность даты и времени резервирования */
    private void validateReservationDateTime(LocalDateTime reservationDateTime) {
        if (reservationDateTime == null) {
            throw new BusinessException("Дата и время резервирования обязательны");
        }
        if (reservationDateTime.isBefore(now(clock).minusMinutes(1))) {
            throw new BusinessException("Нельзя резервировать книгу на прошедшее время");
        }
    }

    /** Считает резервы, которые сейчас занимают экземпляр */
    private long countBlockingReservations(Book book, LocalDateTime now) {
        return reservationRepository.findByBookAndStatus(book, Reservation.STATUS_ACTIVE).stream()
                .filter(item -> !now.isBefore(item.getReservationDate()))
                .filter(item -> !isReservationExpired(item, now))
                .count();
    }

    /** Проверяет, истекло ли окно получения по резерву */
    private boolean isReservationExpired(Reservation reservation, LocalDateTime now) {
        return now.isAfter(reservation.getReservationDate().plusMinutes(RESERVATION_PICKUP_WINDOW_MINUTES));
    }
}
