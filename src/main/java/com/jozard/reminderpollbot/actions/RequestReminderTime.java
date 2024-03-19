package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.MessageService;
import com.jozard.reminderpollbot.StickerService;
import com.jozard.reminderpollbot.users.ChatService;
import com.jozard.reminderpollbot.users.StateMachine;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Component
public class RequestReminderTime extends RequestWithReminderTimeKeyboard {

    public RequestReminderTime(MessageService messageService, ChatService chatService, StickerService stickerService) {
        super(messageService, chatService, stickerService);
    }

    @Override
    protected void doExecute(AbsSender absSender, long chatId, User user, String[] arguments) {
        ChatService.ChatInstance chat = chatService.getChat(chatId).orElseThrow();
        StateMachine state = chat.getStateMachine().orElseThrow();
        if (state.getDays().isEmpty()) {
            if (arguments.length == 0) { // no callback message ID
                logger.error("No callback ID in the RequestReminderTime execute call");
            } else {
                sendAnswerCallbackQuery(absSender, arguments[0]);
            }
        } else {
            state.pendingTime();

            Instant at = Instant.now().truncatedTo(ChronoUnit.MINUTES);
            int hour = at.atZone(ZoneId.of("UTC")).getHour();
            int minute = at.atZone(ZoneId.of("UTC")).getMinute();
            state.setTime(LocalTime.ofInstant(at, ZoneId.of("UTC")));
            InlineKeyboardMarkup keyboardMarkup = markup(chatId, user, hour, minute);
            messageService.send(absSender, chatId,
                    "Reply me with a time in format HH:mm or use the buttons below to select reminder time (UTC).",
                    keyboardMarkup);
        }
    }

    private void sendAnswerCallbackQuery(AbsSender absSender, String callbackQueryId) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQueryId);
        answerCallbackQuery.setShowAlert(false);
        answerCallbackQuery.setText("At least one day of week required");
        try {
            absSender.execute(answerCallbackQuery);
        } catch (Exception e) {
            logger.error("Answer callback query failed", e);
        }

    }
}
