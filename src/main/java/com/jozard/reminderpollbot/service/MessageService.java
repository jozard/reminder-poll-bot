package com.jozard.reminderpollbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class MessageService {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void send(final AbsSender absSender, long chatId, String message) {
        this.send(absSender, chatId, message, ParseMode.MARKDOWN);
    }

    public void send(final AbsSender absSender, long chatId, String message, ReplyKeyboard keyboardMarkup) {
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText(message);
        msg.setParseMode(ParseMode.MARKDOWN);
        if (keyboardMarkup != null) {
            msg.setReplyMarkup(keyboardMarkup);
        }
        try {
            absSender.execute(msg);
        } catch (TelegramApiException e) {
            logger.error("Send message with keyboard failed", e);
        }
    }

    public void send(final AbsSender absSender, long chatId, String message, String parseMode) {
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setParseMode(parseMode);
        msg.setText(message);
        try {
            absSender.execute(msg);
        } catch (TelegramApiException e) {
            logger.error("Send message failed", e);
        }
    }
}
