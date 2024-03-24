package com.jozard.reminderpollbot.commands;


import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.BotCommand;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

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
        if (chatService.getChatState(chatId).isEmpty()) {
            onCommandAction(absSender, chat, user);
        }
    }

}