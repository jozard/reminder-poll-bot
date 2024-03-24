package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import com.jozard.reminderpollbot.service.StateMachine;
import com.jozard.reminderpollbot.service.StickerService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.util.Arrays;

@Component
public class AddAllDaysOfWeek extends Action {
    private final StickerService stickerService;
    private final RequestReminderTime requestReminderTime;


    public AddAllDaysOfWeek(ChatService chatService, StickerService stickerService, MessageService messageService, RequestReminderTime requestReminderTime) {
        super(messageService, chatService);
        this.stickerService = stickerService;
        this.requestReminderTime = requestReminderTime;
    }

    protected void doExecute(AbsSender absSender, StateMachine state, User user, String[] arguments) {
        long chatId = state.getChatId();
        logger.info("User {} adds whole week to a reminder in chat {}",
                user.getUserName() == null ? user.getFirstName() : user.getUserName(), chatId);

        String callbackQueryId = arguments[0];
        if (state.isPendingDays()) {

            state.getDays().addAll(Arrays.stream(DayOfWeek.values()).toList());
            sendAnswerCallbackQuery(absSender, "Whole week added", callbackQueryId);
            this.requestReminderTime.execute(absSender, user, chatId, new String[]{callbackQueryId});
        } else {
            sendAnswerCallbackQuery(absSender,
                    MessageFormat.format(
                            "{0}, you have already been adding/removing a reminder. Answer the last request or use the /start command {1}",
                            user.getUserName(), ":wink:"), callbackQueryId);
        }

    }

}
