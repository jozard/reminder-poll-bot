package com.jozard.reminderpollbot.jobs;

import com.jozard.reminderpollbot.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class StateMachineCleanup implements Runnable {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Long chatId;
    private final ChatService chatService;
    private final AbsSender absSender;

    public StateMachineCleanup(Long chatId, ChatService chatService, AbsSender absSender) {

        this.chatId = chatId;
        this.chatService = chatService;
        this.absSender = absSender;
    }

    @Override
    public void run() {
        boolean chatStateExists = chatService.chatExists(chatId);
        if (chatStateExists) {
            chatService.remove(chatId);
            SendMessage response = new SendMessage();
            response.setChatId(String.valueOf(chatId));
            response.setText("The bot interaction has timed out. Start a new one.");
            try {
                absSender.execute(response);
            } catch (TelegramApiException e) {
                logger.error("Failed to clean up state for chat " + chatId, e);
            }
        }
    }
}
