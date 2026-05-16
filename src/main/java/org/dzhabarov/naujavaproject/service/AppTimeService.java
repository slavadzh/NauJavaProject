package org.dzhabarov.naujavaproject.service;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Время приложения в одной зоне (Екатеринбург): «сейчас» и разбор datetime-local из браузера.
 */
@Service
public class AppTimeService {

    private static final DateTimeFormatter DATETIME_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final ZoneId zoneId;
    private final Clock clock;

    public AppTimeService(ZoneId appZoneId, Clock appClock) {
        this.zoneId = appZoneId;
        this.clock = appClock;
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    public ZoneId getZoneId() {
        return zoneId;
    }

    public String getZoneDisplayName() {
        return zoneId.getId().replace('_', ' ');
    }

    public String formatForDateTimeLocalInput(LocalDateTime dateTime) {
        return dateTime.format(DATETIME_LOCAL);
    }

    public String minReservationDateTimeForInput() {
        return formatForDateTimeLocalInput(now());
    }

    public String formatDisplay(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    /**
     * datetime-local без offset: интерпретируем как время в браузере и сохраняем в зоне приложения.
     *
     * @param browserOffsetMinutes {@link java.util.Date#getTimezoneOffset()} из JavaScript
     */
    public LocalDateTime parseReservationFromBrowser(String dateTimeValue, int browserOffsetMinutes) {
        LocalDateTime naive = LocalDateTime.parse(dateTimeValue, DATETIME_LOCAL);
        ZoneOffset browserOffset = ZoneOffset.ofTotalSeconds((int) (-(long) browserOffsetMinutes * 60));
        return naive.atOffset(browserOffset)
                .atZoneSameInstant(zoneId)
                .toLocalDateTime();
    }
}
