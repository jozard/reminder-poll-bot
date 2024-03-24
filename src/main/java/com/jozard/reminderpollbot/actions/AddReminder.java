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
public class AddReminder extends Action {
    private final StickerService stickerService;
    private final RequestReminderTitle requestReminderTitle;

    public AddReminder(ChatService chatService, StickerService stickerService, MessageService messageService, RequestReminderTitle requestReminderTitle) {
        super(messageService, chatService);
        this.stickerService = stickerService;
        this.requestReminderTitle = requestReminderTitle;
    }

    @Override
    protected void doExecute(AbsSender absSender, StateMachine chatState, User user, String[] arguments) {
        long chatId = chatState.getChatId();
        logger.info("User {} adds reminder to chat {}",
                user.getUserName() == null ? user.getFirstName() : user.getUserName(), chatId);
        StateMachine state = chatService.getOrCreate(chatId, user);
        logger.debug("User state is {}", state.getCurrentState());
        if (state.isNone()) {
            state.setPendingTitle();
            this.requestReminderTitle.execute(absSender, user, chatId, null);
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
