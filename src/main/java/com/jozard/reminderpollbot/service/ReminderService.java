package com.jozard.reminderpollbot.service;

import com.jozard.reminderpollbot.domain.Reminder;
import com.jozard.reminderpollbot.persistence.ReminderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Objects;

@Service
public class ReminderService {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CacheManager cacheManager;
    private final ReminderRepository repo;


    public ReminderService(CacheManager cacheManager, ReminderRepository repo) {
        this.cacheManager = cacheManager;
        this.repo = repo;
    }

    public void add(Reminder reminder) {
        Objects.requireNonNull(cacheManager.getCache("chat_reminder_cache")).evictIfPresent(reminder.getChatId());
        repo.insert(reminder);
    }


    public void remind(AbsSender absSender) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        DayOfWeek dayOfWeek = now.getDayOfWeek();
        int week = now.get(ChronoField.ALIGNED_WEEK_OF_YEAR);
        repo.get(week, dayOfWeek, now.getHour()).stream().filter(
                item -> item.getAt().getMinute() == now.getMinute()).forEach(item -> {
            SendPoll poll = new SendPoll(String.valueOf(item.getChatId()), item.getTitle(),
                    List.of("\uD83D\uDC4D", "\uD83D\uDC4E"));
            poll.setIsAnonymous(false);
            try {
                absSender.execute(poll);
            } catch (TelegramApiException e) {
                logger.error(
                        MessageFormat.format("Failed to show the poll {0} in {1}", item.getTitle(),
                                item.getChatId()),
                        e);
            }

        });
    }

    public boolean hasReminders(long chatId) {
        return !repo.get(chatId).isEmpty();
    }

    public List<String> getTitles(long chatId) {
        return repo.get(chatId).stream().filter(item -> item.getChatId() == chatId).map(Reminder::getTitle).toList();
    }

    public void delete(String title, long chatId, User user) {
        logger.info(MessageFormat.format("Removing {0} from chat ID {1}", title, chatId));
        repo.get(chatId).stream().filter(
                item -> item.getTitle().equals(title.trim()) && item.getChatId() == chatId).findFirst().ifPresentOrElse(
                repo::remove,
                () -> logger.info(MessageFormat.format("Reminder {0} not found for chat ID {1}", title, chatId)));
    }
}
