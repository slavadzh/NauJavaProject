package org.dzhabarov.naujavaproject.entity;

/**
 * Статусы жизненного цикла отчёта
 */
public enum ReportStatus {

    /** Отчёт создан, ожидает генерации */
    CREATED,

    /** Отчёт успешно сформирован */
    COMPLETED,

    /** Ошибка при формировании */
    ERROR
}
