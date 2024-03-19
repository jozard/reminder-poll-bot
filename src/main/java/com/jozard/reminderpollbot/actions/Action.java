package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.MessageService;
import com.jozard.reminderpollbot.users.ChatService;
import com.jozard.reminderpollbot.users.StateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Optional;

abstract public class Action {

    protected final MessageService messageService;
    protected final ChatService chatService;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public Action(MessageService messageService, ChatService chatService) {
        this.chatService = chatService;
        this.messageService = messageService;
    }

    protected abstract void doExecute(AbsSender absSender, long chatId, User user, String[] arguments);

    public void execute(AbsSender absSender, User user, long chatId, String[] arguments) {
        Optional<User> stateUser = chatService.get(chatId, user).getStateMachine().map(StateMachine::getUser);
        if (stateUser.isPresent() && stateUser.get().equals(user)) {
            doExecute(absSender, chatId, user, arguments);
        }
    }
}
