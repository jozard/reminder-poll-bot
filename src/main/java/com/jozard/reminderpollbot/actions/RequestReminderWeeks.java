package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import com.jozard.reminderpollbot.service.StateMachine;
import com.jozard.reminderpollbot.service.StickerService;
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
    protected void doExecute(AbsSender absSender, StateMachine state, User user, String[] arguments) {
        long chatId = state.getChatId();
        int page = Integer.parseInt(arguments[0]);
        InlineKeyboardMarkup keyboardMarkup = markup(chatId, user, page);

        messageService.send(absSender, chatId,
                "Reply me with a comma-separated list of week numbers or use the buttons below.",
                keyboardMarkup);
    }

}
