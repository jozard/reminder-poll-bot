package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import com.jozard.reminderpollbot.service.StateMachine;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.text.MessageFormat;

@Component
public class AddWeek extends Action {

    public AddWeek(ChatService chatService, MessageService messageService) {
        super(messageService, chatService);
    }

    protected void doExecute(AbsSender absSender, StateMachine state, User user, String[] arguments) {
        long chatId = state.getChatId();
        logger.info("User {} adds week to a reminder in chat {}",
                user.getUserName() == null ? user.getFirstName() : user.getUserName(), chatId);

        String callbackQueryId = arguments[1];
        if (state.isPendingWeeks()) {
            int week = Integer.parseInt(arguments[0]);
            state.getWeeks().add(week);
            sendAnswerCallbackQuery(absSender, MessageFormat.format("Week {0} added", week), callbackQueryId);
        } else {
            sendAnswerCallbackQuery(absSender,
                    MessageFormat.format(
                            "{0}, you have already been adding/removing a reminder. Answer the last request or use the /stop command {1}",
                            user.getUserName(), ":wink:"), callbackQueryId);
        }


    }

}
