package com.jozard.reminderpollbot;

import com.jozard.reminderpollbot.users.Reminder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;

@Service
public class ReminderService {


    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final HashSet<Reminder> reminders = new HashSet<>();

    public void add(Reminder reminder) {
        this.reminders.add(reminder);
    }


    public void remind(AbsSender absSender) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        int week = now.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
        LocalTime nowTime = now.toLocalTime().truncatedTo(ChronoUnit.MINUTES);

        reminders.stream().filter(
                item -> item.getWeeks().contains(week) && item.getDays().contains(dayOfWeek) && item.getTime().equals(
                        nowTime)).forEach(item -> {
            SendPoll poll = new SendPoll(String.valueOf(item.getChatId()), item.getTitle(), List.of("Yes", "No"));
            poll.setIsAnonymous(false);
            try {
                absSender.execute(poll);
            } catch (TelegramApiException e) {
                logger.error(
                        MessageFormat.format("Failed to show the poll {0} in {1}", item.getTitle(), item.getChatId()),
                        e);
            }
        });
    }

    public boolean hasReminders() {
        return !reminders.isEmpty();
    }

    public List<String> getTitles(long chatId) {
        return reminders.stream().filter(item -> item.getChatId() == chatId).map(Reminder::getTitle).toList();
    }

    public void delete(String title, long chatId, User user) {
        reminders.stream().filter(
                item -> item.getTitle().equals(title.trim()) && item.getChatId() == chatId).findFirst().ifPresent(
                reminders::remove);
    }
}
