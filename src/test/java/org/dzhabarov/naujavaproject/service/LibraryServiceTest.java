package org.dzhabarov.naujavaproject.service;

import org.dzhabarov.naujavaproject.entity.Book;
import org.dzhabarov.naujavaproject.entity.Loan;
import org.dzhabarov.naujavaproject.entity.Reservation;
import org.dzhabarov.naujavaproject.entity.User;
import org.dzhabarov.naujavaproject.exception.BusinessException;
import org.dzhabarov.naujavaproject.repository.BookRepository;
import org.dzhabarov.naujavaproject.repository.LoanRepository;
import org.dzhabarov.naujavaproject.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты {@link LibraryService}: резерв, выдача, возврат, истечение резервов
 */
@ExtendWith(MockitoExtension.class)
class LibraryServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private ReservationRepository reservationRepository;

    private LibraryService libraryService;

    private Clock clock;
    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        clock = Clock.system(ZoneId.of("Asia/Yekaterinburg"));
        libraryService = new LibraryService(
                bookRepository, loanRepository, reservationRepository, clock);

        user = new User();
        user.setId(1L);
        user.setName("reader");

        book = new Book();
        book.setId(10L);
        book.setTitle("Война и мир");
        book.setTotalCopies(2);
    }

    /**
     * Успешное резервирование при наличии свободного экземпляра
     */
    @Test
    void reserveBook_createsActiveReservation_whenCopiesAvailable() {
        LocalDateTime reservationTime = now().plusDays(1);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(loanRepository.countByBookAndStatus(book, Loan.STATUS_ACTIVE)).thenReturn(0L);
        when(reservationRepository.findByBookAndStatus(book, Reservation.STATUS_ACTIVE)).thenReturn(List.of());
        when(reservationRepository.existsByUserAndBookAndStatus(user, book, Reservation.STATUS_ACTIVE))
                .thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        Reservation result = libraryService.reserveBook(user, 10L, reservationTime);

        assertEquals(Reservation.STATUS_ACTIVE, result.getStatus());
        assertEquals(reservationTime, result.getReservationDate());
        assertEquals(user, result.getUser());
        assertEquals(book, result.getBook());
    }

    /**
     * Нельзя резервировать на прошедшее время
     */
    @Test
    void reserveBook_throws_whenReservationTimeInPast() {
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        LocalDateTime past = now().minusHours(2);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> libraryService.reserveBook(user, 10L, past)
        );
        assertEquals("Нельзя резервировать книгу на прошедшее время", ex.getMessage());
        verify(reservationRepository, never()).save(any());
    }

    /**
     * Нельзя создать второй активный резерв на ту же книгу
     */
    @Test
    void reserveBook_throws_whenUserAlreadyHasActiveReservation() {
        LocalDateTime reservationTime = now().plusDays(1);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(loanRepository.countByBookAndStatus(book, Loan.STATUS_ACTIVE)).thenReturn(0L);
        when(reservationRepository.findByBookAndStatus(book, Reservation.STATUS_ACTIVE)).thenReturn(List.of());
        when(reservationRepository.existsByUserAndBookAndStatus(user, book, Reservation.STATUS_ACTIVE))
                .thenReturn(true);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> libraryService.reserveBook(user, 10L, reservationTime)
        );
        assertEquals("У вас уже есть активное резервирование этой книги", ex.getMessage());
    }

    /**
     * Нельзя резервировать, если все экземпляры заняты
     */
    @Test
    void reserveBook_throws_whenNoCopiesAvailable() {
        book.setTotalCopies(1);
        LocalDateTime reservationTime = now().plusDays(1);
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(loanRepository.countByBookAndStatus(book, Loan.STATUS_ACTIVE)).thenReturn(1L);
        when(reservationRepository.findByBookAndStatus(book, Reservation.STATUS_ACTIVE)).thenReturn(List.of());

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> libraryService.reserveBook(user, 10L, reservationTime)
        );
        assertEquals("Нет доступных экземпляров", ex.getMessage());
    }

    /**
     * Выдача по резерву в открытом окне получения (30 минут)
     */
    @Test
    void takeBook_fulfillsReservation_whenPickupWindowOpen() {
        Reservation reservation = activeReservation(now().minusMinutes(5));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(reservationRepository.findFirstByUserAndBookAndStatus(user, book, Reservation.STATUS_ACTIVE))
                .thenReturn(Optional.of(reservation));
        when(loanRepository.countByBookAndStatus(book, Loan.STATUS_ACTIVE)).thenReturn(0L);
        when(reservationRepository.findByBookAndStatus(book, Reservation.STATUS_ACTIVE))
                .thenReturn(List.of(reservation));
        when(reservationRepository.findByUserAndBook(user, book)).thenReturn(List.of(reservation));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);
            loan.setId(200L);
            return loan;
        });

        Loan loan = libraryService.takeBook(user, 10L);

        assertEquals(Loan.STATUS_ACTIVE, loan.getStatus());
        assertEquals(LocalDate.now().plusWeeks(2), loan.getDueDate());
        assertEquals(Reservation.STATUS_FULFILLED, reservation.getStatus());
        verify(reservationRepository).saveAll(List.of(reservation));
    }

    /**
     * Нельзя забрать книгу до наступления времени резерва
     */
    @Test
    void takeBook_throws_whenReservationTimeNotYetReached() {
        Reservation reservation = activeReservation(now().plusHours(2));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(reservationRepository.findFirstByUserAndBookAndStatus(user, book, Reservation.STATUS_ACTIVE))
                .thenReturn(Optional.of(reservation));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> libraryService.takeBook(user, 10L)
        );
        assertEquals("Забрать книгу можно только после наступления времени резерва", ex.getMessage());
        verify(loanRepository, never()).save(any());
    }

    /**
     * Истёкшее окно получения переводит резерв в EXPIRED
     */
    @Test
    void takeBook_throwsAndExpiresReservation_whenPickupWindowPassed() {
        Reservation reservation = activeReservation(now().minusMinutes(40));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(reservationRepository.findFirstByUserAndBookAndStatus(user, book, Reservation.STATUS_ACTIVE))
                .thenReturn(Optional.of(reservation));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> libraryService.takeBook(user, 10L)
        );
        assertEquals("Время получения по резерву истекло", ex.getMessage());
        assertEquals(Reservation.STATUS_EXPIRED, reservation.getStatus());
        verify(reservationRepository).save(reservation);
    }

    /**
     * Успешная отмена активного резервирования
     */
    @Test
    void cancelReservation_setsCancelledStatus_whenReservationActive() {
        Reservation reservation = activeReservation(now().plusDays(1));
        reservation.setId(50L);
        when(reservationRepository.findByIdAndUser(50L, user)).thenReturn(Optional.of(reservation));

        libraryService.cancelReservation(user, 50L);

        assertEquals(Reservation.STATUS_CANCELLED, reservation.getStatus());
        verify(reservationRepository).save(reservation);
    }

    /**
     * Нельзя отменить резерв не в статусе ACTIVE
     */
    @Test
    void cancelReservation_throws_whenReservationNotActive() {
        Reservation reservation = activeReservation(now().plusDays(1));
        reservation.setId(50L);
        reservation.setStatus(Reservation.STATUS_FULFILLED);
        when(reservationRepository.findByIdAndUser(50L, user)).thenReturn(Optional.of(reservation));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> libraryService.cancelReservation(user, 50L)
        );
        assertEquals("Можно отменять только активное резервирование", ex.getMessage());
    }

    /**
     * Успешный возврат активной выдачи
     */
    @Test
    void returnBook_setsReturnedStatus_whenLoanActive() {
        Loan loan = new Loan();
        loan.setId(300L);
        loan.setStatus(Loan.STATUS_ACTIVE);
        loan.setUser(user);
        when(loanRepository.findByIdAndUser(300L, user)).thenReturn(Optional.of(loan));

        libraryService.returnBook(user, 300L);

        assertEquals(Loan.STATUS_RETURNED, loan.getStatus());
        assertEquals(LocalDate.now(), loan.getReturnDate());
        verify(loanRepository).save(loan);
    }

    /**
     * Нельзя вернуть уже возвращённую книгу
     */
    @Test
    void returnBook_throws_whenLoanAlreadyReturned() {
        Loan loan = new Loan();
        loan.setId(300L);
        loan.setStatus(Loan.STATUS_RETURNED);
        when(loanRepository.findByIdAndUser(300L, user)).thenReturn(Optional.of(loan));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> libraryService.returnBook(user, 300L)
        );
        assertEquals("Книга уже возвращена", ex.getMessage());
    }

    /**
     * Планировщик помечает просроченные резервы как EXPIRED
     */
    @Test
    void expireOutdatedReservations_marksExpiredReservations() {
        Reservation expired = activeReservation(now().minusMinutes(40));
        Reservation active = activeReservation(now().plusDays(1));
        when(reservationRepository.findByStatus(Reservation.STATUS_ACTIVE))
                .thenReturn(List.of(expired, active));

        libraryService.expireOutdatedReservations();

        assertEquals(Reservation.STATUS_EXPIRED, expired.getStatus());
        assertEquals(Reservation.STATUS_ACTIVE, active.getStatus());
        verify(reservationRepository).saveAll(anyList());
    }

    /**
     * Окно получения открыто между временем резерва и дедлайном
     */
    @Test
    void isPickupWindowOpen_returnsTrue_duringPickupWindow() {
        Reservation reservation = activeReservation(now().minusMinutes(10));

        assertTrue(libraryService.isPickupWindowOpen(reservation));
    }

    /**
     * До времени резерва окно получения закрыто
     */
    @Test
    void isBeforePickupWindow_returnsTrue_beforeReservationTime() {
        Reservation reservation = activeReservation(now().plusHours(1));

        assertTrue(libraryService.isBeforePickupWindow(reservation));
        assertFalse(libraryService.isPickupWindowOpen(reservation));
    }

    /**
     * Дедлайн получения — время резерва плюс 30 минут
     */
    @Test
    void getPickupDeadline_addsThirtyMinutesToReservationTime() {
        LocalDateTime reservationTime = LocalDateTime.of(2026, 5, 15, 12, 0);
        Reservation reservation = activeReservation(reservationTime);

        assertEquals(reservationTime.plusMinutes(30), libraryService.getPickupDeadline(reservation));
    }

    /**
     * Книга не найдена — BusinessException
     */
    @Test
    void reserveBook_throws_whenBookNotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> libraryService.reserveBook(user, 999L, now().plusDays(1)));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private Reservation activeReservation(LocalDateTime reservationDate) {
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setReservationDate(reservationDate);
        reservation.setStatus(Reservation.STATUS_ACTIVE);
        return reservation;
    }
}
