package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import com.jozard.reminderpollbot.service.StateMachine;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.text.MessageFormat;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class AddAllWeeks extends Action {
    private final RequestReminderDays requestReminderDays;


    public AddAllWeeks(ChatService chatService, MessageService messageService, RequestReminderDays requestReminderDays) {
        super(messageService, chatService);
        this.requestReminderDays = requestReminderDays;
    }

    protected void doExecute(AbsSender absSender, StateMachine state, User user, String[] arguments) {
        long chatId = state.getChatId();
        logger.info("User {} adds 52 weeks to a reminder in chat {}",
                user.getUserName() == null ? user.getFirstName() : user.getUserName(), chatId);
        String callbackQueryId = arguments[0];
        if (state.isPendingWeeks()) {
            state.getWeeks().addAll(IntStream.rangeClosed(1, 52).boxed().collect(Collectors.toSet()));
            sendAnswerCallbackQuery(absSender, "52 weeks added", callbackQueryId);
            this.requestReminderDays.execute(absSender, user, chatId,
                    new String[]{callbackQueryId});
        } else {
            sendAnswerCallbackQuery(absSender,
                    MessageFormat.format(
                            "{0}, you have already been adding/removing a reminder. Answer the last request or use the /stop command {1}",
                            user.getUserName(), ":wink:"), callbackQueryId);
        }

    }

}
