package com.jozard.reminderpollbot.persistence;

import com.jozard.reminderpollbot.domain.Reminder;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ReminderRowMapper implements RowMapper<Reminder> {
    @Override
    public Reminder mapRow(ResultSet rs, int rowNum) throws SQLException {
        Reminder result = new Reminder();
        result.setId(rs.getLong("id"));
        result.setChatId(rs.getLong("chat_id"));
        result.setOwnerId(rs.getLong("owner_id"));
        result.setTitle(rs.getString("title"));
        result.setWeeks(Arrays.stream(((Integer[]) rs.getArray("weeks").getArray())).collect(Collectors.toSet()));
        result.setDaysOfWeek(
                Arrays.stream(((Integer[]) rs.getArray("days_of_week").getArray())).map(DayOfWeek::of).collect(
                        Collectors.toSet()));
        result.setAt(LocalTime.now(ZoneId.of("UTC"))
                .withHour(rs.getInt("at_hour")).withMinute(rs.getInt("at_minute")));
        return result;
    }
}
