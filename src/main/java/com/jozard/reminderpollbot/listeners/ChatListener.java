package com.jozard.reminderpollbot.listeners;

import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import com.jozard.reminderpollbot.service.StateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Optional;

abstract public class ChatListener {

    protected final MessageService messageService;
    protected final ChatService chatService;
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected ChatListener(MessageService messageService, ChatService chatService) {
        this.messageService = messageService;
        this.chatService = chatService;
    }

    abstract void doExecute(AbsSender absSender, StateMachine state, User user, Message message, String[] arguments);

    public void execute(AbsSender absSender, long chatId, User user, Message message, String[] strings) {
        Optional<StateMachine> state = chatService.getChatState(chatId);
        Optional<User> stateUser = state.map(StateMachine::getUser);
        if (stateUser.isPresent() && stateUser.get().equals(user)) {
            doExecute(absSender, state.get(), user, message, strings);
        }
    }
}
