package org.dzhabarov.naujavaproject.exception;

/**
 * Исключение бизнес-логики с понятным сообщением для пользователя
 */
public class BusinessException extends RuntimeException {

    /**
     * @param message описание ошибки на русском языке
     */
    public BusinessException(String message) {
        super(message);
    }
}
