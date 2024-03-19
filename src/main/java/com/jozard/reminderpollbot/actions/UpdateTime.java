package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.MessageService;
import com.jozard.reminderpollbot.StickerService;
import com.jozard.reminderpollbot.users.ChatService;
import com.jozard.reminderpollbot.users.StateMachine;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalTime;

@Component
public class UpdateTime extends RequestWithReminderTimeKeyboard {

    public UpdateTime(MessageService messageService, ChatService chatService, StickerService stickerService) {
        super(messageService, chatService, stickerService);
    }

    @Override
    protected void doExecute(AbsSender absSender, long chatId, User user, String[] arguments) {
        ChatService.ChatInstance chat = chatService.getChat(chatId).orElseThrow();
        StateMachine state = chat.getStateMachine().orElseThrow();

        TIME_BUTTONS buttonPressed = TIME_BUTTONS.from(arguments[0]).orElseThrow();
        int messageId = Integer.parseInt(arguments[1]);
        String inlineMessageId = arguments[2];
        LocalTime time = state.getTime();
        logger.debug("Current time:  {}", time);
        state.setTime(switch (buttonPressed) {
            case HOURS_INC -> time.plusHours(1);
            case HOURS_DEC -> time.minusHours(1);
            case MINUTES_INC -> time.plusMinutes(1);
            case MINUTES_DEC -> time.minusMinutes(1);
        });
        logger.info("New time: {}", time);
        InlineKeyboardMarkup keyboardMarkup = markup(chatId, user, state.getTime().getHour(),
                state.getTime().getMinute());
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup(String.valueOf(chatId), messageId,
                inlineMessageId,
                keyboardMarkup);
        try {
            absSender.execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            logger.error("Failed to update the time keyboard", e);
        }

    }

}
