package com.jozard.reminderpollbot.commands;


import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

public abstract class UserAwareCommand extends Command {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public UserAwareCommand(MessageService messageService, ChatService chatService, String name, String description) {
        super(messageService, chatService, name, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        long chatId = chat.getId();
        User stateUser = chatService.getOrCreate(chatId, user).getUser();
        if (stateUser.equals(user)) {
            this.onCommandAction(absSender, chat, user);
        }
    }

}