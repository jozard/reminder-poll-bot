package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.domain.Reminder;
import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import com.jozard.reminderpollbot.service.ReminderService;
import com.jozard.reminderpollbot.service.StateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.text.MessageFormat;

@Component
public class CreateReminder extends Action {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ReminderService reminderService;

    public CreateReminder(ChatService chatService, MessageService messageService, ReminderService reminderService) {
        super(messageService, chatService);
        this.reminderService = reminderService;
    }

    @Override
    protected void doExecute(AbsSender absSender, StateMachine state, User user, String[] arguments) {
        long chatId = state.getChatId();
        logger.info("User {} adds week to a reminder in chat {}",
                user.getUserName() == null ? user.getFirstName() : user.getUserName(), chatId);
        User stateUser = state.getUser();
        if (stateUser.equals(user)) {

            if (state.isPendingTime()) {
                Reminder.ReminderBuilder reminder = new Reminder.ReminderBuilder(state.getTitle(), chatId,
                        user.getId());
                reminder.setWeeks(state.getWeeks());
                reminder.setDaysOfWeek(state.getDays());
                reminder.setAt(state.getTime());
                reminderService.add(reminder.build());
                chatService.remove(chatId);
                ReplyKeyboardRemove keyboardRemove = ReplyKeyboardRemove.builder().removeKeyboard(true).build();
                messageService.send(absSender, chatId, "Reminder created", keyboardRemove);
            } else {
                if (arguments.length > 0) {
                    String callbackQueryId = arguments[0];
                    sendAnswerCallbackQuery(absSender,
                            MessageFormat.format(
                                    "{0}, you have already been adding/removing a reminder. Answer the last request or use the /start command {1}",
                                    user.getUserName(), ":wink:"), callbackQueryId);
                } else {
                    logger.info(
                            MessageFormat.format("Create reminder failed. User {0} is in the {1} state",
                                    user.getUserName() == null ? user.getFirstName() : user.getUserName(),
                                    state.getCurrentState()));

                }
            }

        }
    }
}
