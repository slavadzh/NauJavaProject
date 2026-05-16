package org.dzhabarov.naujavaproject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Единый часовой пояс приложения: сравнение времени резерва и «сейчас»
 * должно совпадать с тем, что пользователь выбирает в datetime-local.
 */
@Configuration
public class TimeConfig {

    @Bean
    public ZoneId appZoneId(@Value("${app.time-zone:Asia/Yekaterinburg}") String timeZone) {
        return ZoneId.of(timeZone);
    }

    @Bean
    public Clock appClock(ZoneId appZoneId) {
        return Clock.system(appZoneId);
    }

    /** Задаёт JVM time zone (важно для Docker, где по умолчанию часто UTC) */
    @Bean
    public ApplicationRunner configureDefaultTimeZone(ZoneId appZoneId) {
        return args -> TimeZone.setDefault(TimeZone.getTimeZone(appZoneId));
    }

    /** Текущее локальное время в зоне приложения */
    public static LocalDateTime now(Clock clock) {
        return LocalDateTime.now(clock);
    }
}
