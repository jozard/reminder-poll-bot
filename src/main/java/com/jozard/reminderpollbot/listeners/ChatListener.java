package com.jozard.reminderpollbot.listeners;

import com.jozard.reminderpollbot.MessageService;
import com.jozard.reminderpollbot.users.ChatService;
import com.jozard.reminderpollbot.users.StateMachine;
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

    abstract void doExecute(AbsSender absSender, ChatService.ChatInstance chat, User user, Message message, String[] arguments);

    public void execute(AbsSender absSender, long chatId, User user, Message message, String[] strings) {
        Optional<ChatService.ChatInstance> chat = chatService.getChat(chatId);
        Optional<User> stateUser = chat.flatMap(ChatService.ChatInstance::getStateMachine).map(StateMachine::getUser);
        if (stateUser.isPresent() && stateUser.get().equals(user)) {
            doExecute(absSender, chat.get(), user, message, strings);
        }
    }
}
