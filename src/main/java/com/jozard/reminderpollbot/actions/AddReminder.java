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

@Component
public class AddReminder extends Action {
    private final StickerService stickerService;
    private final RequestReminderTitle requestReminderTitle;

    public AddReminder(ChatService chatService, StickerService stickerService, MessageService messageService, RequestReminderTitle requestReminderTitle) {
        super(messageService, chatService);
        this.stickerService = stickerService;
        this.requestReminderTitle = requestReminderTitle;
    }

    @Override
    protected void doExecute(AbsSender absSender, long chatId, User user, String[] arguments) {
        logger.info("User {} adds reminder to chat {}",
                user.getUserName() == null ? user.getFirstName() : user.getUserName(), chatId);

        StateMachine state = chatService.get(chatId, user).getStateMachine().orElseThrow();
        logger.debug("User state is {}", state.getCurrentState());
        if (state.isPendingTitle()) {
            this.requestReminderTitle.execute(absSender, user, chatId, null);
        } else {
            try {
                sendAnswerCallbackQuery(absSender,
                        MessageFormat.format(
                                "{0}, you have already been adding/removing a reminder. Answer the last request or use the /start command {1}",
                                user.getUserName(), ":wink:"), arguments[0]);
            } catch (TelegramApiException e) {

            }
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
