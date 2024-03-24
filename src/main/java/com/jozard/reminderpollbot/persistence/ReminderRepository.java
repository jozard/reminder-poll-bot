package com.jozard.reminderpollbot.persistence;

import com.jozard.reminderpollbot.domain.Reminder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;

@Repository
public class ReminderRepository {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final CacheManager cacheManager;

    public ReminderRepository(DataSource dataSource, CacheManager cacheManager) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("reminder").usingGeneratedKeyColumns("id");
        this.cacheManager = cacheManager;
    }

    @Cacheable(value = "chat_reminders", key = "#chatId")
    public List<Reminder> get(Long chatId) {
        return this.jdbcTemplate.query("select * from reminder where chat_id = :chat_id",
                Map.of("chat_id", chatId),
                new ReminderRowMapper());
    }

    @Cacheable(value = "week_day_reminders", key = "{#week, #dayOfWeek.getValue(), #at_hour}")
    public List<Reminder> get(int week, DayOfWeek dayOfWeek, int at_hour) {
        logger.info(MessageFormat.format("Get reminders for week {0} and {1} 1 hour after {2}", week, dayOfWeek,
                at_hour + ":00"));
        return this.jdbcTemplate.query(
                "select * from reminder where :week = any (weeks) and :day_of_week = any (days_of_week) and at_hour = :at_hour",
                Map.of("week", week, "day_of_week", dayOfWeek.getValue(), "at_hour", at_hour), new ReminderRowMapper());
    }

    @Caching(evict = {@CacheEvict(value = "chat_reminders", key = "#reminder.getChatId()"),
            @CacheEvict(value = "week_day_reminders", allEntries = true)})
    public void remove(Reminder reminder) {
        this.jdbcTemplate.update("delete from reminder where id = :id", Map.of("id", reminder.getId()));
    }


    @Caching(evict = {@CacheEvict(value = "chat_reminders", key = "#reminder.getChatId()"),
            @CacheEvict(value = "week_day_reminders", allEntries = true)})
    public void insert(Reminder reminder) {
        try {
            MapSqlParameterSource parameterSource = new MapSqlParameterSource();
            parameterSource.addValue("title", reminder.getTitle());
            parameterSource.addValue("chat_id", reminder.getChatId());
            parameterSource.addValue("owner_id", reminder.getOwnerId());

            Connection connection = this.jdbcTemplate.getJdbcTemplate().getDataSource().getConnection();
            Array weeks = connection.createArrayOf("integer", reminder.getWeeks().toArray());
            Array daysOfWeek = connection.createArrayOf("integer",
                    reminder.getDaysOfWeek().stream().map(DayOfWeek::getValue).toArray());

            parameterSource.addValue("weeks", weeks, Types.ARRAY);
            parameterSource.addValue("days_of_week", daysOfWeek, Types.ARRAY);
            parameterSource.addValue("at_hour", reminder.getAt().getHour());
            parameterSource.addValue("at_minute", reminder.getAt().getMinute());
            jdbcInsert.execute(parameterSource);
        } catch (SQLException e) {
            logger.error("Failed to insert reminder", e);
        }
    }
}
