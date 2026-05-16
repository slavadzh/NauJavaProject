package org.dzhabarov.naujavaproject.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Разбор datetime-local с учётом offset браузера и зоны приложения (Екатеринбург).
 */
class AppTimeServiceTest {

    private AppTimeService appTimeService;

    @BeforeEach
    void setUp() {
        ZoneId ekb = ZoneId.of("Asia/Yekaterinburg");
        Clock clock = Clock.system(ekb);
        appTimeService = new AppTimeService(ekb, clock);
    }

    @Test
    void parseReservationFromBrowser_keepsTime_whenBrowserInEkb() {
        int ekbOffsetMinutes = -300;

        LocalDateTime result = appTimeService.parseReservationFromBrowser(
                "2026-05-16T15:00", ekbOffsetMinutes);

        assertEquals(LocalDateTime.of(2026, 5, 16, 15, 0), result);
    }

    @Test
    void parseReservationFromBrowser_shiftsFromMoscowToEkb() {
        int moscowOffsetMinutes = -180;

        LocalDateTime result = appTimeService.parseReservationFromBrowser(
                "2026-05-16T15:00", moscowOffsetMinutes);

        assertEquals(LocalDateTime.of(2026, 5, 16, 17, 0), result);
    }
}
