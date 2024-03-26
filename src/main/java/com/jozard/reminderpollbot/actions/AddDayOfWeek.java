package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import com.jozard.reminderpollbot.service.StateMachine;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;

@Component
public class AddDayOfWeek extends Action {

    public AddDayOfWeek(ChatService chatService, MessageService messageService) {
        super(messageService, chatService);
    }

    @Override
    protected void doExecute(AbsSender absSender, StateMachine state, User user, String[] arguments) {
        long chatId = state.getChatId();
        logger.info("User {} adds day to a reminder in chat {}",
                user.getUserName() == null ? user.getFirstName() : user.getUserName(), chatId);


        String callbackQueryId = arguments[1];
        if (state.isPendingDays()) {
            DayOfWeek day = DayOfWeek.of(Integer.parseInt(arguments[0]));
            String messageId = arguments[2];
            String inlineMessageId = arguments[3];
            state.getDays().add(day);
            sendAnswerCallbackQuery(absSender, MessageFormat.format("{0} added",
                    day.getDisplayName(TextStyle.FULL, Locale.of(user.getLanguageCode()))), callbackQueryId);
        } else {
            sendAnswerCallbackQuery(absSender,
                    MessageFormat.format(
                            "{0}, you have already been adding/removing a reminder. Answer the last request or use the /stop command {1}",
                            user.getUserName(), ":wink:"), callbackQueryId);
        }


    }

}
