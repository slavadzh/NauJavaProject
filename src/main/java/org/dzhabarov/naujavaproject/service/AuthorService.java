package org.dzhabarov.naujavaproject.service;

import lombok.extern.slf4j.Slf4j;
import org.dzhabarov.naujavaproject.entity.Author;
import org.dzhabarov.naujavaproject.exception.BusinessException;
import org.dzhabarov.naujavaproject.repository.AuthorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Бизнес-логика авторов: CRUD и привязка к книгам
 */
@Slf4j
@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    /**
     * @param authorRepository репозиторий авторов
     */
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    /** Возвращает всех авторов */
    public List<Author> findAll() {
        return authorRepository.findAll();
    }

    /** Создаёт нового автора */
    public Author create(String name, String country, LocalDate birthDate) {
        validateBirthDate(birthDate);
        Author author = new Author();
        author.setName(name);
        author.setCountry(country);
        author.setBirthDate(birthDate);
        Author saved = authorRepository.save(author);
        log.info("Author created: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    /** Обновляет данные автора */
    public Author update(Long id, String name, String country, LocalDate birthDate) {
        validateBirthDate(birthDate);
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Автор не найден"));
        author.setName(name);
        author.setCountry(country);
        author.setBirthDate(birthDate);
        Author saved = authorRepository.save(author);
        log.info("Author updated: id={}", saved.getId());
        return saved;
    }

    /** Удаляет автора */
    public void delete(Long id) {
        if (!authorRepository.existsById(id)) {
            throw new BusinessException("Автор не найден");
        }
        authorRepository.deleteById(id);
        log.info("Author deleted: id={}", id);
    }

    /** Находит авторов по списку идентификаторов для привязки к книге */
    public Set<Author> resolveAuthors(Set<Long> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(authorRepository.findAllById(authorIds));
    }

    /** Проверяет, что дата рождения не в будущем */
    private void validateBirthDate(LocalDate birthDate) {
        if (birthDate != null && birthDate.isAfter(LocalDate.now())) {
            throw new BusinessException("Дата рождения не может быть в будущем");
        }
    }
}
