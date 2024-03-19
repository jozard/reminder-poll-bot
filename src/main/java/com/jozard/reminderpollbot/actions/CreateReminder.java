package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.MessageService;
import com.jozard.reminderpollbot.ReminderService;
import com.jozard.reminderpollbot.users.ChatService;
import com.jozard.reminderpollbot.users.Reminder;
import com.jozard.reminderpollbot.users.StateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.MessageFormat;
import java.util.Optional;

@Component
public class CreateReminder extends Action {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ReminderService reminderService;

    public CreateReminder(ChatService chatService, MessageService messageService, ReminderService reminderService) {
        super(messageService, chatService);
        this.reminderService = reminderService;
    }

    private void sendAnswerCallbackQuery(AbsSender absSender, String message, String callbackQueryId) throws TelegramApiException {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQueryId);
        answerCallbackQuery.setShowAlert(false);
        answerCallbackQuery.setText(message);
        absSender.execute(answerCallbackQuery);
    }

    @Override
    protected void doExecute(AbsSender absSender, long chatId, User user, String[] arguments) {
        logger.info("User {} adds week to a reminder in chat {}",
                user.getUserName() == null ? user.getFirstName() : user.getUserName(), chatId);
        Optional<User> stateUser = chatService.get(chatId, user).getStateMachine().map(StateMachine::getUser);
        if (stateUser.isPresent() && stateUser.get().equals(user)) {
            StateMachine state = chatService.get(chatId, user).getStateMachine().orElseThrow();
            try {
                if (state.isPendingTime()) {
                    Reminder reminder = new Reminder(state.getTitle(), user, chatId);
                    reminder.setWeeks(state.getWeeks());
                    reminder.setDays(state.getDays());
                    reminder.setTime(state.getTime());
                    reminderService.add(reminder);
                    chatService.remove(chatId);
                    ReplyKeyboardRemove keyboardRemove = ReplyKeyboardRemove.builder().removeKeyboard(true).build();
                    messageService.send(absSender, chatId, "Reminder created", keyboardRemove);
                } else {
                    String callbackQueryId = arguments[1];
                    sendAnswerCallbackQuery(absSender,
                            MessageFormat.format(
                                    "{0}, you have already been adding/removing a reminder. Answer the last request or use the /start command {1}",
                                    user.getUserName(), ":wink:"), callbackQueryId);
                }
            } catch (TelegramApiException e) {
                logger.error("Failed to create a reminder", e);
            }
        }
    }
}
