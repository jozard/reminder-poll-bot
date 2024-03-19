package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.MessageService;
import com.jozard.reminderpollbot.StickerService;
import com.jozard.reminderpollbot.users.ChatService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
public class RequestReminderTitle extends Action {

    private final StickerService stickerService;

    public RequestReminderTitle(ChatService chatService, StickerService stickerService, MessageService messageService) {
        super(messageService, chatService);
        this.stickerService = stickerService;

    }

    @Override
    protected void doExecute(AbsSender absSender, long chatId, User user, String[] strings) {
        messageService.send(absSender, chatId,
                """
                        Reply to this message with a poll title for the new reminder.
                        For example, "Who is going to the gym today?\"""");
    }
}
