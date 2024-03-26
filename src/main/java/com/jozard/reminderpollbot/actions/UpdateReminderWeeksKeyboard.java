package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import com.jozard.reminderpollbot.service.StateMachine;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class UpdateReminderWeeksKeyboard extends RequestWithReminderWeeksKeyboard {

    public UpdateReminderWeeksKeyboard(ChatService chatService, MessageService messageService) {
        super(chatService, messageService);
    }

    @Override
    protected void doExecute(AbsSender absSender, StateMachine state, User user, String[] arguments) {
        long chatId = state.getChatId();
        int page = Integer.parseInt(arguments[0]);
        int messageId = Integer.parseInt(arguments[1]);
        String inlineMessageId = arguments[2];
        InlineKeyboardMarkup keyboardMarkup = markup(chatId, user, page);
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup(String.valueOf(chatId), messageId,
                inlineMessageId,
                keyboardMarkup);
        try {
            absSender.execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            logger.error("Failed to update reminder weeks keyboard", e);
        }
    }

}
