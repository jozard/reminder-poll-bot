package com.jozard.reminderpollbot.commands;


import com.jozard.reminderpollbot.MessageService;
import com.jozard.reminderpollbot.users.ChatService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Optional;

@Component
public class Stop extends UserAwareCommand {

    public static final String NAME = "stop";
    public static final String DESCRIPTION = """
            With this command you can cancel any ongoing activity with this bot, for example, a new poll creation.
            """;

    public Stop(MessageService messageService, ChatService chatService) {
        super(messageService, chatService, NAME, DESCRIPTION);
    }

    @Override
    void onCommandAction(AbsSender absSender, Chat chat, User user) {
        var chatId = chat.getId();
        if (chatService.chatExists(chatId)) {
            Optional<ChatService.ChatInstance> chatInstance = chatService.getChat(chatId);
            chatInstance.ifPresent(value -> chatService.remove(chatId));
        }
    }

}