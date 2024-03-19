package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.MessageService;
import com.jozard.reminderpollbot.users.ChatService;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public abstract class RequestWithReminderWeeksKeyboard extends Action {


    public RequestWithReminderWeeksKeyboard(ChatService chatService, MessageService messageService) {
        super(messageService, chatService);
    }

    protected InlineKeyboardMarkup markup(long chatId, User user, int page) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> keyboardRowWeeks = new ArrayList<>();
        List<InlineKeyboardButton> keyboardRowExtra = new ArrayList<>();

        InlineKeyboardButton prevButton = InlineKeyboardButton.builder().text("<<").callbackData(
                "btn_weeks_page_" + (page == 0 ? 12 : page - 1)).build();

        InlineKeyboardButton nextButton = InlineKeyboardButton.builder().text(">>").callbackData(
                "btn_weeks_page_" + (page == 12 ? 0 : page + 1)).build();
        
        keyboardRowWeeks.add(prevButton);
        for (int i = 1; i < 5; i++) {
            int week = page * 4 + i;
            keyboardRowWeeks.add(InlineKeyboardButton.builder().text(String.valueOf(week)).callbackData(
                    "btn_week_" + week).build());
        }
        keyboardRowWeeks.add(nextButton);
        keyboardRowExtra.add(InlineKeyboardButton.builder().text("All").callbackData("btn_weeks_all").build());
        keyboardRowExtra.add(InlineKeyboardButton.builder().text("Done").callbackData("btn_weeks_done").build());

        keyboardMarkup.setKeyboard(List.of(keyboardRowWeeks, keyboardRowExtra));
        return keyboardMarkup;
    }

}
