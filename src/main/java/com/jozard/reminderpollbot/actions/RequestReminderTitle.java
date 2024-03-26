package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import com.jozard.reminderpollbot.service.StateMachine;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
public class RequestReminderTitle extends Action {
    public RequestReminderTitle(ChatService chatService, MessageService messageService) {
        super(messageService, chatService);
    }

    @Override
    protected void doExecute(AbsSender absSender, StateMachine state, User user, String[] arguments) {
        long chatId = state.getChatId();
        messageService.send(absSender, chatId,
                """
                        Reply me with a poll title for the new reminder. For example, "Who is going to the gym today?".
                        The poll will be shown with the Yes/No options so that the chat participants could answer it.""");
    }
}
