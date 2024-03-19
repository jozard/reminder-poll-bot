package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.MessageService;
import com.jozard.reminderpollbot.ReminderService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
public class ShowReminderList {
    private static final String DELIMITER = System.getProperty("line.separator");
    private final MessageService messageService;
    private final ReminderService reminderService;

    public ShowReminderList(MessageService messageService, ReminderService reminderService) {
        this.messageService = messageService;

        this.reminderService = reminderService;
    }

    public void execute(AbsSender absSender, long chatId, User user, String[] arguments) {
        messageService.send(absSender, chatId,
                String.join(DELIMITER,
                        reminderService.getTitles(chatId).stream().map(item -> "\"" + item + "\"").toList()));
    }

}
