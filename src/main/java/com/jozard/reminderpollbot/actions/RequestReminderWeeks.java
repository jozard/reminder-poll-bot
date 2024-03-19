package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.MessageService;
import com.jozard.reminderpollbot.StickerService;
import com.jozard.reminderpollbot.users.ChatService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
public class RequestReminderWeeks extends RequestWithReminderWeeksKeyboard {

    private final StickerService stickerService;

    public RequestReminderWeeks(ChatService chatService, StickerService stickerService, MessageService messageService) {
        super(chatService, messageService);
        this.stickerService = stickerService;
    }

    @Override
    protected void doExecute(AbsSender absSender, long chatId, User user, String[] arguments) {
        int page = Integer.parseInt(arguments[0]);
        InlineKeyboardMarkup keyboardMarkup = markup(chatId, user, page);

        messageService.send(absSender, chatId,
                "Reply me with a comma-separated list of week numbers or use the buttons below.",
                keyboardMarkup);
    }

}
