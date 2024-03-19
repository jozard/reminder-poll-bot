package com.jozard.reminderpollbot.listeners;

import com.jozard.reminderpollbot.MessageService;
import com.jozard.reminderpollbot.ReminderService;
import com.jozard.reminderpollbot.users.ChatService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
public class OnDeleteTitleSent extends ChatListener {

    private final ReminderService reminderService;

    public OnDeleteTitleSent(MessageService messageService, ChatService chatService, ReminderService reminderService) {
        super(messageService, chatService);
        this.reminderService = reminderService;
    }

    @Override
    void doExecute(AbsSender absSender, ChatService.ChatInstance chat, User user, Message message, String[] arguments) {
        // reminder title sent
        logger.info(
                "The user {} is pending title for delete in chatInstance ID =  {}. We assume it is a title in the message",
                user.getFirstName(), chat.getChatId());

        String title = message.getText();
        reminderService.delete(title, chat.getChatId(), user);
        chatService.remove(chat.getChatId());
    }
}
