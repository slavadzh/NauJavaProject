package org.dzhabarov.naujavaproject.service;

import org.dzhabarov.naujavaproject.entity.Author;
import org.dzhabarov.naujavaproject.exception.BusinessException;
import org.dzhabarov.naujavaproject.repository.AuthorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты {@link AuthorService}: CRUD и валидация даты рождения
 */
@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorService authorService;

    /**
     * Успешное создание автора
     */
    @Test
    void create_savesAuthor_whenBirthDateValid() {
        LocalDate birthDate = LocalDate.of(1828, 9, 9);
        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> {
            Author author = invocation.getArgument(0);
            author.setId(1L);
            return author;
        });

        Author result = authorService.create("Толстой", "Россия", birthDate);

        assertEquals("Толстой", result.getName());
        assertEquals("Россия", result.getCountry());
        assertEquals(birthDate, result.getBirthDate());
    }

    /**
     * Дата рождения в будущем запрещена
     */
    @Test
    void create_throws_whenBirthDateInFuture() {
        LocalDate future = LocalDate.now().plusDays(1);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> authorService.create("Автор", "Страна", future)
        );
        assertEquals("Дата рождения не может быть в будущем", ex.getMessage());
        verify(authorRepository, never()).save(any());
    }

    /**
     * Удаление несуществующего автора — ошибка
     */
    @Test
    void delete_throws_whenAuthorNotFound() {
        when(authorRepository.existsById(99L)).thenReturn(false);

        assertThrows(BusinessException.class, () -> authorService.delete(99L));
        verify(authorRepository, never()).deleteById(99L);
    }

    /**
     * Обновление несуществующего автора — ошибка
     */
    @Test
    void update_throws_whenAuthorNotFound() {
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> authorService.update(99L, "Имя", "Страна", null)
        );
    }

    /**
     * Пустой список id авторов даёт пустое множество
     */
    @Test
    void resolveAuthors_returnsEmptySet_whenIdsNullOrEmpty() {
        assertTrue(authorService.resolveAuthors(null).isEmpty());
        assertTrue(authorService.resolveAuthors(Set.of()).isEmpty());
        verify(authorRepository, never()).findAllById(any());
    }

    /**
     * resolveAuthors загружает авторов по id
     */
    @Test
    void resolveAuthors_returnsAuthorsFromRepository() {
        Author author = new Author();
        author.setId(5L);
        author.setName("Чехов");
        when(authorRepository.findAllById(Set.of(5L))).thenReturn(List.of(author));

        Set<Author> result = authorService.resolveAuthors(Set.of(5L));

        assertEquals(1, result.size());
        assertEquals("Чехов", result.iterator().next().getName());
    }
}
