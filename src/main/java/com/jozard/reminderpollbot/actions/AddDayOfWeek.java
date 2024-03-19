package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.MessageService;
import com.jozard.reminderpollbot.StickerService;
import com.jozard.reminderpollbot.users.ChatService;
import com.jozard.reminderpollbot.users.StateMachine;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;

@Component
public class AddDayOfWeek extends Action {
    private final StickerService stickerService;

    public AddDayOfWeek(ChatService chatService, StickerService stickerService, MessageService messageService) {
        super(messageService, chatService);
        this.stickerService = stickerService;
    }

    @Override
    protected void doExecute(AbsSender absSender, long chatId, User user, String[] arguments) {
        logger.info("User {} adds day to a reminder in chat {}",
                user.getUserName() == null ? user.getFirstName() : user.getUserName(), chatId);

        StateMachine state = chatService.get(chatId, user).getStateMachine().orElseThrow();
        try {
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
                                "{0}, you have already been adding/removing a reminder. Answer the last request or use the /start command {1}",
                                user.getUserName(), ":wink:"), callbackQueryId);
            }
        } catch (TelegramApiException e) {

        }

    }

    private void sendAnswerCallbackQuery(AbsSender absSender, String message, String callbackQueryId) throws TelegramApiException {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQueryId);
        answerCallbackQuery.setShowAlert(false);
        answerCallbackQuery.setText(message);
        absSender.execute(answerCallbackQuery);
    }
}
