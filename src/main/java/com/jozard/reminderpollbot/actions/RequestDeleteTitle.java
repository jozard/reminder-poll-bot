package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import com.jozard.reminderpollbot.service.StateMachine;
import com.jozard.reminderpollbot.service.StickerService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.text.MessageFormat;

@Component
public class RequestDeleteTitle extends Action {

    private final StickerService stickerService;

    public RequestDeleteTitle(ChatService chatService, StickerService stickerService, MessageService messageService) {
        super(messageService, chatService);
        this.stickerService = stickerService;

    }

    @Override
    protected void doExecute(AbsSender absSender, StateMachine state, User user, String[] arguments) {
        long chatId = state.getChatId();
        logger.debug("User state is {}", state.getCurrentState());
        if (state.isNone()) {
            state.setPendingDeleteTitle();
            messageService.send(absSender, chatId,
                    """
                            Reply to this message with a poll title to be removed""");
        } else {

            sendAnswerCallbackQuery(absSender,
                    MessageFormat.format(
                            """
                                    {0}, you have already been adding/removing a reminder.
                                     Answer the last request or use the /start command {1}""", user.getUserName(),
                            ":wink:"), arguments[0]);

        }

    }
}
