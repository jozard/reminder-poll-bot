package com.jozard.reminderpollbot.commands;


import com.jozard.reminderpollbot.MessageService;
import com.jozard.reminderpollbot.users.ChatService;
import com.jozard.reminderpollbot.users.StateMachine;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Optional;

public abstract class UserAwareCommand extends Command {

    public UserAwareCommand(MessageService messageService, ChatService chatService, String name, String description) {
        super(messageService, chatService, name, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        long chatId = chat.getId();
        Optional<User> stateUser = chatService.get(chatId, user).getStateMachine().map(StateMachine::getUser);
        if (stateUser.isPresent() && stateUser.get().equals(user)) {
            super.execute(absSender, user, chat, strings);
        }
    }

}