package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.MessageService;
import com.jozard.reminderpollbot.StickerService;
import com.jozard.reminderpollbot.users.ChatService;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class RequestWithReminderTimeKeyboard extends Action {
    private final StickerService stickerService;

    public RequestWithReminderTimeKeyboard(MessageService messageService, ChatService chatService, StickerService stickerService) {
        super(messageService, chatService);
        this.stickerService = stickerService;
    }

    protected InlineKeyboardMarkup markup(long chatId, User user, int hour, int minute) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> keyboardRowTop = new ArrayList<>();
        List<InlineKeyboardButton> keyboardRowLabels = new ArrayList<>();
        List<InlineKeyboardButton> keyboardRowBottom = new ArrayList<>();
        List<InlineKeyboardButton> keyboardRowDone = new ArrayList<>();

        keyboardRowTop.add(
                InlineKeyboardButton.builder().text(Character.toString((char) 708)).callbackData(
                        TIME_BUTTONS.HOURS_INC.value()).build());
        keyboardRowTop.add(
                InlineKeyboardButton.builder().text(Character.toString((char) 708)).callbackData(
                        TIME_BUTTONS.MINUTES_INC.value()).build());
        LocalTime time = LocalTime.of(hour, minute);
        keyboardRowLabels.add(
                InlineKeyboardButton.builder().text(DateTimeFormatter.ofPattern("HH : mm").format(time)).callbackData(
                        "btn_hours_label").build());
        keyboardRowBottom.add(
                InlineKeyboardButton.builder().text(Character.toString((char) 709)).callbackData(
                        TIME_BUTTONS.HOURS_DEC.value()).build());
        keyboardRowBottom.add(
                InlineKeyboardButton.builder().text(Character.toString((char) 709)).callbackData(
                        TIME_BUTTONS.MINUTES_DEC.value()).build());
        keyboardRowDone.add(
                InlineKeyboardButton.builder().text("Done").callbackData("btn_time_done").build());

        keyboardMarkup.setKeyboard(List.of(keyboardRowTop, keyboardRowLabels, keyboardRowBottom, keyboardRowDone));
        return keyboardMarkup;
    }

    protected enum TIME_BUTTONS {
        HOURS_INC("btn_time_hours_inc"),
        HOURS_DEC("btn_time_hours_dec"),
        MINUTES_INC("btn_time_minutes_inc"),
        MINUTES_DEC("btn_time_minutes_dec");
        private final String value;


        TIME_BUTTONS(String value) {
            this.value = value;
        }

        public static Optional<TIME_BUTTONS> from(String value) {
            return Arrays.stream(values()).filter(button -> button.value().equals(value)).findFirst();
        }

        public String value() {
            return value;
        }
    }


}
