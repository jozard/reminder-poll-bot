package com.jozard.reminderpollbot.listeners;

import com.jozard.reminderpollbot.MessageService;
import com.jozard.reminderpollbot.actions.CreateReminder;
import com.jozard.reminderpollbot.users.ChatService;
import com.jozard.reminderpollbot.users.StateMachine;
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

    private final CreateReminder createReminder;

    public OnTimeSent(MessageService messageService, ChatService chatService, CreateReminder createReminder) {
        super(messageService, chatService);
        this.createReminder = createReminder;
    }

    @Override
    void doExecute(AbsSender absSender, ChatService.ChatInstance chat, User user, Message message, String[] arguments) {
        // reminder weeks sent
        logger.info(
                "The user {} is pending reminder time in chatInstance ID =  {}. We assume it is a time in the message",
                user.getFirstName(), chat.getChatId());
        try {
            LocalTime timeSent = LocalTime.parse(message.getText().trim());

            StateMachine state = chat.getStateMachine().orElseThrow();
            state.setTime(LocalTime.ofInstant(Instant.now(), ZoneId.of("UTC")).withHour(timeSent.getHour()).withMinute(
                    timeSent.getMinute()).truncatedTo(ChronoUnit.MINUTES));

            logger.info("Time {} added by the user {}", state.getTime(), user.getUserName());
            this.createReminder.execute(absSender, user, chat.getChatId(), new String[]{});
        } catch (DateTimeParseException e) {
            ReplyKeyboardRemove keyboardRemove = ReplyKeyboardRemove.builder().removeKeyboard(false).build();
            messageService.send(absSender, chat.getChatId(),
                    "The response must be a UTC time in format HH:mm",
                    keyboardRemove);
        }

    }

}
