package com.jozard.reminderpollbot.commands;


import com.jozard.reminderpollbot.MessageService;
import com.jozard.reminderpollbot.users.ChatService;
import com.jozard.reminderpollbot.users.StateMachine;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Optional;

public abstract class Command extends BotCommand implements IBotCommand {

    protected final MessageService messageService;
    protected final ChatService chatService;

    public Command(MessageService messageService, ChatService chatService, String name, String description) {
        super(name, description);
        this.chatService = chatService;
        this.messageService = messageService;
    }

    abstract void onCommandAction(AbsSender absSender, Chat chat, User user);

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        long chatId = chat.getId();
        Optional<User> stateUser = chatService.get(chatId, user).getStateMachine().map(StateMachine::getUser);
        if (stateUser.isEmpty()) {
            onCommandAction(absSender, chat, user);
        }
    }

}