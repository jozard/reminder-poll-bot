package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.MessageService;
import com.jozard.reminderpollbot.StickerService;
import com.jozard.reminderpollbot.users.ChatService;
import com.jozard.reminderpollbot.users.StateMachine;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
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
    private final StickerService stickerService;

    public RequestReminderDays(MessageService messageService, ChatService chatService, StickerService stickerService) {
        super(messageService, chatService);
        this.stickerService = stickerService;
    }

    @Override
    protected void doExecute(AbsSender absSender, long chatId, User user, String[] arguments) {
        ChatService.ChatInstance chat = chatService.getChat(chatId).orElseThrow();
        StateMachine state = chat.getStateMachine().orElseThrow();
        if (state.getWeeks().isEmpty()) {
            if (arguments.length == 0) { // no callback message ID
                sendAtLeastOneWeekRequired(absSender, chat);
            } else {
                sendAnswerCallbackQuery(absSender, arguments[0]);
            }
        } else {
            state.pendingDays();
            InlineKeyboardMarkup keyboardMarkup = markup(chatId, user);
            messageService.send(absSender, chatId,
                    "Use the buttons below to select days of week.",
                    keyboardMarkup);
        }
    }

    private InlineKeyboardMarkup markup(long chatId, User user) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> keyboardRow = new ArrayList<>();
        List<InlineKeyboardButton> keyboardRowExtra = new ArrayList<>();
        Arrays.stream(DayOfWeek.values()).forEach(dayOfWeek -> {
            keyboardRow.add(InlineKeyboardButton.builder().text(
                    dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.of(user.getLanguageCode()))).callbackData(
                    "btn_day_of_week_" + dayOfWeek.getValue()).build());
        });
        keyboardRowExtra.add(
                InlineKeyboardButton.builder().text("All").callbackData("btn_day_of_week_all").build());
        keyboardRowExtra.add(
                InlineKeyboardButton.builder().text("Done").callbackData("btn_day_of_week_done").build());

        keyboardMarkup.setKeyboard(List.of(keyboardRow, keyboardRowExtra));
        return keyboardMarkup;
    }

    private void sendAtLeastOneWeekRequired(AbsSender absSender, ChatService.ChatInstance chat) {
        ReplyKeyboardRemove keyboardRemove = ReplyKeyboardRemove.builder().removeKeyboard(false).build();
        messageService.send(absSender, chat.getChatId(), AT_LEAST_ONE_WEEK_NUMBER_REQUIRED,
                keyboardRemove);
    }

    private void sendAnswerCallbackQuery(AbsSender absSender, String callbackQueryId) {
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQueryId);
        answerCallbackQuery.setShowAlert(false);
        answerCallbackQuery.setText(RequestReminderDays.AT_LEAST_ONE_WEEK_NUMBER_REQUIRED);
        try {
            absSender.execute(answerCallbackQuery);
        } catch (Exception e) {
            logger.error("Answer callback query failed", e);
        }

    }
}
