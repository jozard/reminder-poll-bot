package com.jozard.reminderpollbot.users;

import org.telegram.telegrambots.meta.api.objects.User;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Reminder {
    private final String title;
    private final User owner;
    private final long chatId;
    private LocalTime time;
    private Set<Integer> weeks = new HashSet<>();
    private Set<DayOfWeek> days = new HashSet<>();

    public Reminder(String title, User owner, long chatId) {
        this.title = title;
        this.owner = owner;
        this.chatId = chatId;
    }

    public long getChatId() {
        return chatId;
    }

    public String getTitle() {
        return title;
    }

    public User getOwner() {
        return owner;
    }


    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public Set<Integer> getWeeks() {
        return weeks;
    }

    public void setWeeks(Set<Integer> weeks) {
        this.weeks = weeks;
    }

    public Set<DayOfWeek> getDays() {
        return days;
    }

    public void setDays(Set<DayOfWeek> days) {
        this.days = days;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reminder reminder = (Reminder) o;
        return chatId == reminder.chatId && Objects.equals(title, reminder.title) && Objects.equals(
                owner, reminder.owner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, owner, chatId);
    }
}
