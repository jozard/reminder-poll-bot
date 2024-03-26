package com.jozard.reminderpollbot.commands;


import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import com.jozard.reminderpollbot.service.StateMachine;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Optional;

@Component
public class Stop extends UserAwareCommand {

    public static final String NAME = "stop";
    public static final String DESCRIPTION = """
            With this command you can cancel any ongoing interaction with the bot.
            """;

    public Stop(MessageService messageService, ChatService chatService) {
        super(messageService, chatService, NAME, DESCRIPTION);
    }

    @Override
    void onCommandAction(AbsSender absSender, Chat chat, User user) {
        logger.info("User {} stops bot in {} chat {}", user.getFirstName(),
                chat.isGroupChat() ? "group" : "private", chat.getId());
        var chatId = chat.getId();
        if (chatService.chatExists(chatId)) {
            Optional<StateMachine> state = chatService.getChatState(chatId);
            state.ifPresentOrElse(value -> chatService.remove(chatId),
                    () -> logger.info("State not found for chat {}", chat.getId()));
        }
    }

}