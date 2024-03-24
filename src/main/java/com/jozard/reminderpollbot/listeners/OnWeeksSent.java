package com.jozard.reminderpollbot.listeners;

import com.jozard.reminderpollbot.actions.RequestReminderDays;
import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import com.jozard.reminderpollbot.service.StateMachine;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Arrays;
import java.util.List;

@Component
public class OnWeeksSent extends ChatListener {

    private final RequestReminderDays requestReminderDays;

    public OnWeeksSent(MessageService messageService, ChatService chatService, RequestReminderDays requestReminderDays) {
        super(messageService, chatService);
        this.requestReminderDays = requestReminderDays;
    }

    @Override
    void doExecute(AbsSender absSender, StateMachine state, User user, Message message, String[] arguments) {
        long chatId = state.getChatId();
        // reminder weeks sent
        logger.info(
                "The user {} is pending reminder weeks in chatInstance ID =  {}. We assume it is a weeks list in the message",
                user.getFirstName(), chatId);

        String weeks = message.getText();

        if ("done".equals(weeks)) {
            this.requestReminderDays.execute(absSender, user, chatId, new String[]{});
        } else {
            List<String> weeksList = Arrays.stream(weeks.split(",")).map(String::trim).toList();
            if (weeksList.isEmpty() || weeksList.stream().anyMatch(week -> {
                try {
                    int weekNumber = Integer.parseInt(week);
                    if (weekNumber < 1 || weekNumber > 52) {
                        return true;
                    }
                } catch (NumberFormatException e) {
                    return true;
                }
                return false;
            })) {
                ReplyKeyboardRemove keyboardRemove = ReplyKeyboardRemove.builder().removeKeyboard(false).build();
                messageService.send(absSender, chatId,
                        "The response must be a comma-separated list of week numbers",
                        keyboardRemove);
            }
            for (String week : weeksList) {
                state.getWeeks().add(Integer.parseInt(week));
            }
            logger.info("Weeks {} added by the user {}. Current weeks are {}", weeksList, user.getUserName(),
                    state.getWeeks());
            ReplyKeyboardRemove keyboardRemove = ReplyKeyboardRemove.builder().removeKeyboard(true).build();
            messageService.send(absSender, chatId, "Weeks added", keyboardRemove);
            this.requestReminderDays.execute(absSender, user, chatId, new String[]{});
        }

    }

}
