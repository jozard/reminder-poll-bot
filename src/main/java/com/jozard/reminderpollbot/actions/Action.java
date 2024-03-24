package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import com.jozard.reminderpollbot.service.StateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

abstract public class Action {

    protected final MessageService messageService;
    protected final ChatService chatService;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public Action(MessageService messageService, ChatService chatService) {
        this.chatService = chatService;
        this.messageService = messageService;
    }

    protected abstract void doExecute(AbsSender absSender, StateMachine state, User user, String[] arguments);

    public void execute(AbsSender absSender, User user, long chatId, String[] arguments) {
        StateMachine state = chatService.getOrCreate(chatId, user);
        if (state.getUser().equals(user)) {
            doExecute(absSender, state, user, arguments);
        }
    }

    protected void sendAnswerCallbackQuery(AbsSender absSender, String message, String callbackQueryId) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQueryId);
        answerCallbackQuery.setShowAlert(false);
        answerCallbackQuery.setText(message);
        try {
            absSender.execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            logger.error("Answer callback query failed", e);
        }
    }
}
