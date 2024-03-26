package com.jozard.reminderpollbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

public class StateMachine {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final User user;
    private final Long chatId;
    private final Set<Integer> weeks = new HashSet<>();
    private final Set<DayOfWeek> days = new HashSet<>();
    private ReminderState currentState = ReminderState.NONE;
    private String title;
    private LocalTime time;

    private ScheduledFuture<?> cleanupTask;

    public StateMachine(User user, Long chatId) {
        this.user = user;
        this.chatId = chatId;
    }

    public String getTitle() {
        return title;
    }

    public User user() {
        return user;
    }


    public User getUser() {
        return user;
    }

    public ReminderState getCurrentState() {
        return currentState;
    }

    public Long getChatId() {
        return chatId;
    }

    public boolean isPendingTitle() {
        return currentState.equals(ReminderState.PENDING_TITLE);
    }

    public boolean isNone() {
        return this.currentState.equals(ReminderState.NONE);
    }

    public void setPendingTitle() {
        this.currentState = ReminderState.PENDING_TITLE;
        logger.info("Set state to " + ReminderState.PENDING_TITLE);
    }

    public void pendingWeeks(String title) {
        this.title = title;
        this.currentState = ReminderState.PENDING_WEEKS;
        logger.info("Set state to " + ReminderState.PENDING_WEEKS);
    }

    public boolean isPendingWeeks() {
        return this.currentState == ReminderState.PENDING_WEEKS;
    }

    public boolean isPendingDays() {
        return this.currentState == ReminderState.PENDING_DAYS_OF_WEEK;
    }

    public boolean isPendingTime() {
        return this.currentState == ReminderState.PENDING_TIME;
    }

    public boolean isPendingDeleteTitle() {
        return this.currentState == ReminderState.PENDING_DELETE_TITLE;
    }

    public Set<Integer> getWeeks() {
        return weeks;
    }

    public Set<DayOfWeek> getDays() {
        return days;
    }

    public void pendingDays() {
        this.currentState = ReminderState.PENDING_DAYS_OF_WEEK;
        logger.info("Set state to " + ReminderState.PENDING_DAYS_OF_WEEK);
    }

    public void pendingTime() {
        this.currentState = ReminderState.PENDING_TIME;
        logger.info("Set state to " + ReminderState.PENDING_TIME);
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;

    }

    public void setPendingDeleteTitle() {
        this.currentState = ReminderState.PENDING_DELETE_TITLE;
    }

    public ScheduledFuture<?> getCleanupTask() {
        return cleanupTask;
    }

    public void setCleanupTask(ScheduledFuture<?> cleanupTask) {
        this.cleanupTask = cleanupTask;
    }

    public enum ReminderState {
        NONE, PENDING_TITLE,

        PENDING_WEEKS,

        PENDING_DAYS_OF_WEEK, DAYS_OF_WEEK_SET, PENDING_TIME,

        PENDING_DELETE_TITLE, TIME_SET
    }
}
