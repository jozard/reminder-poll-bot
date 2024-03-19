package com.jozard.reminderpollbot.listeners;

import com.jozard.reminderpollbot.MessageService;
import com.jozard.reminderpollbot.actions.RequestReminderWeeks;
import com.jozard.reminderpollbot.users.ChatService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
public class OnReminderTitleSent extends ChatListener {

    private final RequestReminderWeeks requestReminderWeeks;

    public OnReminderTitleSent(MessageService messageService, ChatService chatService, RequestReminderWeeks requestReminderWeeks) {
        super(messageService, chatService);
        this.requestReminderWeeks = requestReminderWeeks;
    }

    @Override
    void doExecute(AbsSender absSender, ChatService.ChatInstance chat, User user, Message message, String[] arguments) {
        // reminder title sent
        logger.info(
                "The user {} is pending reminder title in chatInstance ID =  {}. We assume it is a title in the message",
                user.getFirstName(), chat.getChatId());

        String title = message.getText();
        chat.getStateMachine().orElseThrow().pendingWeeks(title);
        this.requestReminderWeeks.execute(absSender, user, chat.getChatId(), new String[]{"0"});
    }
}
