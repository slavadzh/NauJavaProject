package org.dzhabarov.naujavaproject.service;

import org.dzhabarov.naujavaproject.dto.BookDTO;
import org.dzhabarov.naujavaproject.entity.Author;
import org.dzhabarov.naujavaproject.entity.Book;
import org.dzhabarov.naujavaproject.entity.Loan;
import org.dzhabarov.naujavaproject.entity.Reservation;
import org.dzhabarov.naujavaproject.exception.BusinessException;
import org.dzhabarov.naujavaproject.mapper.BookMapper;
import org.dzhabarov.naujavaproject.repository.BookRepository;
import org.dzhabarov.naujavaproject.repository.LoanRepository;
import org.dzhabarov.naujavaproject.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты {@link BookService}: CRUD, поиск, расчёт доступных экземпляров
 */
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private ReservationRepository reservationRepository;

    private BookService bookService;

    private Clock clock;
    private Book book;
    private BookDTO bookDto;

    @BeforeEach
    void setUp() {
        clock = Clock.system(ZoneId.of("Asia/Yekaterinburg"));
        bookService = new BookService(
                bookRepository, bookMapper, loanRepository, reservationRepository, clock);

        book = new Book();
        book.setId(1L);
        book.setTitle("Преступление и наказание");
        book.setGenre("Роман");
        book.setPublicationYear(1866);
        book.setTotalCopies(3);

        Author author = new Author();
        author.setName("Достоевский");
        book.setAuthors(Set.of(author));

        bookDto = new BookDTO();
        bookDto.setId(1L);
        bookDto.setTitle(book.getTitle());
        bookDto.setTotalCopies(3);
    }

    /**
     * Успешное создание книги с валидным числом экземпляров
     */
    @Test
    void create_savesBook_whenCopiesValid() {
        when(bookRepository.save(book)).thenReturn(book);

        Book saved = bookService.create(book);

        assertEquals(book, saved);
        verify(bookRepository).save(book);
    }

    /**
     * Отрицательное число экземпляров запрещено
     */
    @Test
    void create_throws_whenCopiesNegative() {
        book.setTotalCopies(-1);

        BusinessException ex = assertThrows(BusinessException.class, () -> bookService.create(book));
        assertEquals("Количество экземпляров не может быть отрицательным", ex.getMessage());
        verify(bookRepository, never()).save(any());
    }

    /**
     * Удаление несуществующей книги — ошибка
     */
    @Test
    void delete_throws_whenBookNotFound() {
        when(bookRepository.existsById(99L)).thenReturn(false);

        assertThrows(BusinessException.class, () -> bookService.delete(99L));
        verify(bookRepository, never()).deleteById(99L);
    }

    /**
     * Обновление несуществующей книги — ошибка
     */
    @Test
    void update_throws_whenBookNotFound() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> bookService.update(99L, book));
    }

    /**
     * Комбинированный поиск фильтрует по названию и жанру
     */
    @Test
    void search_filtersByTitleAndGenre() {
        Book other = new Book();
        other.setId(2L);
        other.setTitle("Анна Каренина");
        other.setGenre("Роман");
        other.setTotalCopies(1);

        when(bookRepository.findAll()).thenReturn(List.of(book, other));
        when(bookMapper.toBookDTO(any(Book.class))).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            BookDTO dto = new BookDTO();
            dto.setId(b.getId());
            dto.setTitle(b.getTitle());
            return dto;
        });
        when(loanRepository.countByBookAndStatus(any(), eq(Loan.STATUS_ACTIVE))).thenReturn(0L);
        when(reservationRepository.findByBookAndStatus(any(), eq(Reservation.STATUS_ACTIVE)))
                .thenReturn(List.of());

        List<BookDTO> result = bookService.search("преступление", null, "роман", null);

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().getId());
    }

    /**
     * Доступные экземпляры = всего минус выдачи минус блокирующие резервы
     */
    @Test
    void findAll_calculatesAvailableCopies() {
        Reservation blocking = new Reservation();
        blocking.setReservationDate(LocalDateTime.now(clock).minusMinutes(5));
        blocking.setStatus(Reservation.STATUS_ACTIVE);

        when(bookRepository.findAll()).thenReturn(List.of(book));
        when(bookMapper.toBookDTO(book)).thenReturn(bookDto);
        when(loanRepository.countByBookAndStatus(book, Loan.STATUS_ACTIVE)).thenReturn(1L);
        when(reservationRepository.findByBookAndStatus(book, Reservation.STATUS_ACTIVE))
                .thenReturn(List.of(blocking));

        List<BookDTO> result = bookService.findAll();

        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getAvailableCopies());
    }

    /**
     * Поиск по автору находит книгу с совпадающим именем автора
     */
    @Test
    void search_filtersByAuthorName() {
        when(bookRepository.findAll()).thenReturn(List.of(book));
        when(bookMapper.toBookDTO(book)).thenReturn(bookDto);
        when(loanRepository.countByBookAndStatus(any(), eq(Loan.STATUS_ACTIVE))).thenReturn(0L);
        when(reservationRepository.findByBookAndStatus(any(), eq(Reservation.STATUS_ACTIVE)))
                .thenReturn(List.of());

        List<BookDTO> result = bookService.search(null, "достоевский", null, null);

        assertEquals(1, result.size());
    }

    /**
     * Пустой фильтр не отсекает книги
     */
    @Test
    void search_returnsAll_whenFiltersEmpty() {
        when(bookRepository.findAll()).thenReturn(List.of(book));
        when(bookMapper.toBookDTO(book)).thenReturn(bookDto);
        when(loanRepository.countByBookAndStatus(any(), eq(Loan.STATUS_ACTIVE))).thenReturn(0L);
        when(reservationRepository.findByBookAndStatus(any(), eq(Reservation.STATUS_ACTIVE)))
                .thenReturn(List.of());

        List<BookDTO> result = bookService.search(null, null, null, null);

        assertEquals(1, result.size());
    }
}
