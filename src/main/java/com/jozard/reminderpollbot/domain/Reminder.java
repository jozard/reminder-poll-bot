package com.jozard.reminderpollbot.domain;


import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Reminder {
    private long id;

    private String title;

    private Long chatId;

    private Long ownerId;

    private Set<Integer> weeks;

    private Set<DayOfWeek> daysOfWeek;
    private LocalTime at;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Set<Integer> getWeeks() {
        return weeks;
    }

    public void setWeeks(Set<Integer> weeks) {
        this.weeks = weeks;
    }

    public Set<DayOfWeek> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(Set<DayOfWeek> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    public LocalTime getAt() {
        return at;
    }

    public void setAt(LocalTime at) {
        this.at = at;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reminder reminder = (Reminder) o;
        return Objects.equals(title, reminder.title) && Objects.equals(chatId,
                reminder.chatId) && Objects.equals(ownerId, reminder.ownerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, chatId, ownerId);
    }

    public static class ReminderBuilder {
        private String title;
        private Long chatId;
        private Long ownerId;
        private Set<Integer> weeks;
        private Set<DayOfWeek> daysOfWeek;
        private LocalTime at;

        public ReminderBuilder() {
        }

        public ReminderBuilder(String title, Long chatId, Long ownerId) {
            this.title = title;
            this.chatId = chatId;
            this.ownerId = ownerId;
        }

        public ReminderBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public ReminderBuilder setChatId(Long chatId) {
            this.chatId = chatId;
            return this;
        }

        public ReminderBuilder setOwnerId(Long ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public ReminderBuilder setWeeks(Collection<Integer> weeks) {
            this.weeks = new HashSet<>(weeks);
            return this;
        }

        public ReminderBuilder setDaysOfWeek(Collection<DayOfWeek> daysOfWeek) {
            this.daysOfWeek = new HashSet<>(daysOfWeek);
            return this;
        }

        public ReminderBuilder setAt(LocalTime at) {
            this.at = at;
            return this;
        }

        public Reminder build() {
            Reminder result = new Reminder();
            result.setTitle(this.title);
            result.setChatId(this.chatId);
            result.setOwnerId(this.ownerId);
            result.setWeeks(new HashSet<>(this.weeks));
            result.setDaysOfWeek(new HashSet<>(this.daysOfWeek));
            result.setAt(this.at);
            return result;
        }
    }
}
