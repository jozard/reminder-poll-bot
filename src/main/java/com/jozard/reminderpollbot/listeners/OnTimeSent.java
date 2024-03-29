package com.jozard.reminderpollbot.listeners;

import com.jozard.reminderpollbot.actions.SubmitReminder;
import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import com.jozard.reminderpollbot.service.StateMachine;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

@Component
public class OnTimeSent extends ChatListener {

    private final SubmitReminder submitReminder;

    public OnTimeSent(MessageService messageService, ChatService chatService, SubmitReminder submitReminder) {
        super(messageService, chatService);
        this.submitReminder = submitReminder;
    }


    @Override
    void doExecute(AbsSender absSender, StateMachine state, User user, Message message, String[] arguments) {
        long chatId = state.getChatId();
        // reminder weeks sent
        logger.info(
                "The user {} is pending reminder time in chatInstance ID =  {}. We assume it is a time in the message",
                user.getFirstName(), chatId);
        try {
            LocalTime timeSent = LocalTime.parse(message.getText().trim());
            state.setTime(LocalTime.ofInstant(Instant.now(), ZoneId.of("UTC")).withHour(timeSent.getHour()).withMinute(
                    timeSent.getMinute()).truncatedTo(ChronoUnit.MINUTES));

            logger.info("Time {} added by the user {}", state.getTime(), user.getUserName());
            this.submitReminder.execute(absSender, user, chatId, new String[]{});
        } catch (DateTimeParseException e) {
            ReplyKeyboardRemove keyboardRemove = ReplyKeyboardRemove.builder().removeKeyboard(false).build();
            messageService.send(absSender, chatId,
                    "The response must be a UTC time in format HH:mm",
                    keyboardRemove);
        }
    }
}
