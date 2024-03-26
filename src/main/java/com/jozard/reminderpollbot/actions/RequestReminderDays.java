package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import com.jozard.reminderpollbot.service.StateMachine;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class RequestReminderDays extends Action {

    private static final String AT_LEAST_ONE_WEEK_NUMBER_REQUIRED = "At least one week number required";

    public RequestReminderDays(MessageService messageService, ChatService chatService) {
        super(messageService, chatService);
    }

    @Override
    protected void doExecute(AbsSender absSender, StateMachine state, User user, String[] arguments) {
        long chatId = state.getChatId();
        if (state.getWeeks().isEmpty()) {
            if (arguments.length == 0) { // no callback message ID
                sendAtLeastOneWeekRequired(absSender, state);
            } else {
                sendAnswerCallbackQuery(absSender, RequestReminderDays.AT_LEAST_ONE_WEEK_NUMBER_REQUIRED, arguments[0]);
            }
        } else {
            state.pendingDays();
            InlineKeyboardMarkup keyboardMarkup = markup(chatId, user);
            messageService.send(absSender, chatId,
                    """
                            Use the buttons below to select days of week.
                            The poll will be shown at these days.""",
                    keyboardMarkup);
        }
    }

    private InlineKeyboardMarkup markup(long chatId, User user) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> keyboardRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardRowExtra = new ArrayList<>();
        Arrays.stream(DayOfWeek.values()).forEach(dayOfWeek -> keyboardRow.add(InlineKeyboardButton.builder().text(
                dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.of(user.getLanguageCode()))).callbackData(
                "btn_day_of_week_" + dayOfWeek.getValue()).build()));
        keyboardRowExtra.add(
                InlineKeyboardButton.builder().text("All").callbackData("btn_day_of_week_all").build());
        keyboardRowExtra.add(
                InlineKeyboardButton.builder().text("Done").callbackData("btn_day_of_week_done").build());

        keyboardMarkup.setKeyboard(List.of(keyboardRow, keyboardRowExtra));
        return keyboardMarkup;
    }

    private void sendAtLeastOneWeekRequired(AbsSender absSender, StateMachine stateMachine) {
        ReplyKeyboardRemove keyboardRemove = ReplyKeyboardRemove.builder().removeKeyboard(false).build();
        messageService.send(absSender, stateMachine.getChatId(), AT_LEAST_ONE_WEEK_NUMBER_REQUIRED,
                keyboardRemove);
    }

}
