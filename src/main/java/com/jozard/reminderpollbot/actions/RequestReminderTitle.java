package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import com.jozard.reminderpollbot.service.StateMachine;
import com.jozard.reminderpollbot.service.StickerService;
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
    protected void doExecute(AbsSender absSender, StateMachine state, User user, String[] arguments) {
        long chatId = state.getChatId();
        messageService.send(absSender, chatId,
                """
                        Reply to this message with a poll title for the new reminder.
                        For example, "Who is going to the gym today?\"""");
    }
}
