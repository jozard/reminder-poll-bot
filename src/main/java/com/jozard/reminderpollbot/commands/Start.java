package com.jozard.reminderpollbot.commands;


import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import com.jozard.reminderpollbot.service.ReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.ArrayList;
import java.util.List;

@Component
public class Start extends Command {

    public static final String NAME = "start";
    public static final String DESCRIPTION = """
            With this command you can show the bot actions available.""";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ReminderService reminderService;

    public Start(MessageService messageService, ChatService chatService, ReminderService reminderService) {
        super(messageService, chatService, NAME, DESCRIPTION);
        this.reminderService = reminderService;
    }

    @Override
    void onCommandAction(AbsSender absSender, Chat chat, User user) {

        logger.debug("User {} starts bot in {} chat {}", user.getFirstName(),
                chat.isGroupChat() ? "group" : "private", chat.getId());
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> keyboardRow = new ArrayList<>();

        InlineKeyboardButton addButton = InlineKeyboardButton.builder().text("Add").callbackData(
                "btn_add").build();
        InlineKeyboardButton removeButton = InlineKeyboardButton.builder().text("Remove").callbackData(
                "btn_remove").build();
        InlineKeyboardButton listButton = InlineKeyboardButton.builder().text("List").callbackData(
                "btn_list").build();
        keyboardRow.add(addButton);
        if (reminderService.hasReminders(chat.getId())) {
            keyboardRow.add(removeButton);
            keyboardRow.add(listButton);
        }
        keyboardMarkup.setKeyboard(List.of(keyboardRow));
        messageService.send(absSender, chat.getId(),
                "Manage recurring reminder polls", keyboardMarkup);


    }
}