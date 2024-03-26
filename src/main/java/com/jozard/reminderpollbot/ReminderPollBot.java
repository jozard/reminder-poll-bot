package com.jozard.reminderpollbot;

import com.jozard.reminderpollbot.actions.*;
import com.jozard.reminderpollbot.commands.Command;
import com.jozard.reminderpollbot.commands.Start;
import com.jozard.reminderpollbot.commands.Stop;
import com.jozard.reminderpollbot.listeners.OnDeleteTitleSent;
import com.jozard.reminderpollbot.listeners.OnReminderTitleSent;
import com.jozard.reminderpollbot.listeners.OnTimeSent;
import com.jozard.reminderpollbot.listeners.OnWeeksSent;
import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.ReminderService;
import com.jozard.reminderpollbot.service.StateMachine;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberBanned;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberMember;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReminderPollBot extends TelegramLongPollingCommandBot implements ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private final ChatService chatService;
    private final AddReminder addReminder;
    private final AddWeek addWeek;
    private final AddAllWeeks addAllWeeks;
    private final AddDayOfWeek addDayOfWeek;
    private final AddAllDaysOfWeek addAllDaysOfWeek;
    private final RequestReminderWeeks requestReminderWeeks;
    private final SubmitReminder submitReminder;
    private final RequestReminderDays requestReminderDays;
    private final RequestReminderTime requestReminderTime;
    private final UpdateReminderWeeksKeyboard updateReminderWeeksKeyboard;
    private final OnReminderTitleSent onReminderTitleSent;
    private final OnWeeksSent onWeeksSent;
    private final OnTimeSent onTimeSent;
    private final OnDeleteTitleSent onDeleteTitleSent;
    private final Logger logger = LoggerFactory.getLogger(ReminderPollBot.class);
    private final UpdateTime updateTime;

    private final ReminderService reminderService;
    private final ShowReminderList showReminderList;
    private final RequestDeleteTitle requestDeleteTitle;

    public ReminderPollBot(@Value("${botConfig.token}") String token, ChatService chatService, Start start, Stop stop, AddReminder addReminder, AddWeek addWeek, AddAllWeeks addAllWeeks, AddDayOfWeek addDayOfWeek, AddAllDaysOfWeek addAllDaysOfWeek, RequestReminderWeeks requestReminderWeeks, SubmitReminder submitReminder, RequestReminderDays requestReminderDays, RequestReminderTime requestReminderTime, UpdateReminderWeeksKeyboard updateReminderWeeksKeyboard, OnReminderTitleSent onReminderTitleSent, OnWeeksSent onWeeksSent, OnTimeSent onTimeSent, OnDeleteTitleSent onDeleteTitleSent, UpdateTime updateTime, ReminderService reminderService, ShowReminderList showReminderList, RequestDeleteTitle requestDeleteTitle) throws TelegramApiException {
        super(new DefaultBotOptions(), true, token);
        this.chatService = chatService;
        this.addReminder = addReminder;
        this.addWeek = addWeek;
        this.addAllWeeks = addAllWeeks;
        this.addDayOfWeek = addDayOfWeek;
        this.addAllDaysOfWeek = addAllDaysOfWeek;
        this.requestReminderWeeks = requestReminderWeeks;
        this.submitReminder = submitReminder;
        this.requestReminderDays = requestReminderDays;
        this.requestReminderTime = requestReminderTime;
        this.updateReminderWeeksKeyboard = updateReminderWeeksKeyboard;
        this.onReminderTitleSent = onReminderTitleSent;
        this.onWeeksSent = onWeeksSent;
        this.onTimeSent = onTimeSent;
        this.onDeleteTitleSent = onDeleteTitleSent;
        this.updateTime = updateTime;
        this.reminderService = reminderService;
        this.showReminderList = showReminderList;
        this.requestDeleteTitle = requestDeleteTitle;
    }

    @PostConstruct
    public void registerCommands() {
        var commands = ReminderPollBot.applicationContext.getBeansOfType(Command.class).values();

        var botCommands = commands
                .stream()
                .collect(Collectors.toMap(Command::getCommandIdentifier, Command::getDescription))
                .entrySet()
                .stream()
                .map(entry -> new BotCommand(entry.getKey(), entry.getValue()))
                .toList();

        var setCommands = SetMyCommands
                .builder()
                .commands(botCommands)
                .build();
        try {
            execute(setCommands);
        } catch (TelegramApiException e) {
            logger.error("Couldn't update commands for the menu button. " + e);
        }
        logger.info("Registering commands {}", setCommands);
        registerAll(commands.toArray(Command[]::new));
    }

    @Override
    public String getBotUsername() {
        return "reminder_poll_bot";
    }

    @Override
    public void processInvalidCommandUpdate(Update update) {
        super.processInvalidCommandUpdate(update);
        logger.error(MessageFormat.format("Invalid command {0} from {1}", update.getMessage().getText(),
                update.getMessage().getFrom()));
    }


    @Override
    public void onUpdatesReceived(List<Update> updates) {
        logger.debug("Updates received");
        updates.forEach(update -> {
            try {
                preprocessUpdate(update);
            } catch (Exception e) {
                logger.error("Updated preprocess failed: ", e);
            }
        });
        super.onUpdatesReceived(updates);
    }

    private void preprocessUpdate(Update update) {
        logger.debug("Preprocess update: " + (update.hasMessage() ? update.getMessage().getText() : "no message"));
        if (update.hasMyChatMember()) {
            // bot is blocked/unblocked by the user
            ChatMemberUpdated memberUpdated = update.getMyChatMember();
            User newUser = memberUpdated.getNewChatMember().getUser();
            String newStatus = memberUpdated.getNewChatMember().getStatus();
            // verify it is the movie bot
            if (newUser.getUserName().equals(getBotUsername())) {
                if (newStatus.equals(ChatMemberBanned.STATUS)) {
                    chatService.remove(memberUpdated.getChat().getId());
                    logger.debug(
                            MessageFormat.format("The bot was blocked in chat {0}", memberUpdated.getChat().getId()));
                } else if (newStatus.equals(ChatMemberMember.STATUS)) {
                    // unfortunately we have no info about the user here
                }
            }
        }
    }

    @Override
    public void processNonCommandUpdate(Update update) {
        logger.debug("Process non command updated");
        if (update.hasMessage()) {
            Message message = update.getMessage();
            logger.debug(MessageFormat.format("Process message {0} from {1}", message.getText(),
                    message.getFrom().getUserName()));
            // a message from the user. Let's process it.
            var user = message.getFrom();
            Long chatId = message.getChatId();
            Optional<StateMachine> state = chatService.getChatState(chatId);
            if (state.isEmpty()) {
                // there are no ongoing activity in this chat. Ignore this message,
                return;
            }
            if (state.get().isPendingTitle()) {
                this.onReminderTitleSent.execute(this, chatId, user, message, null);
            } else if (state.get().isPendingWeeks()) {
                this.onWeeksSent.execute(this, chatId, user, message, null);
            } else if (state.get().isPendingDays()) {
                // do nothing
            } else if (state.get().isPendingTime()) {
                this.onTimeSent.execute(this, chatId, user, message, null);
            } else if (state.get().isPendingDeleteTitle()) {
                this.onDeleteTitleSent.execute(this, chatId, user, message, null);

            } else {
                //the user is done. Ignore
                logger.info("User {} is in DONE state. Ignore this message", user.getFirstName());
            }

        } else if (update.hasCallbackQuery()) {
            // User clicked a button
            CallbackQuery callbackQuery = update.getCallbackQuery();
            logger.debug(MessageFormat.format("Process callback query {0} from {1}", callbackQuery.getData(),
                    callbackQuery.getFrom().getUserName()));
            User user = callbackQuery.getFrom();
            Long chatId = callbackQuery.getMessage().getChatId();

            if (callbackQuery.getData().equals("btn_add")) {
                logger.debug("Add button callback received");
                this.addReminder.execute(this, user, chatId, new String[]{callbackQuery.getId()});
            } else if (callbackQuery.getData().equals("btn_remove")) {
                logger.debug("Remove button callback received");
                this.requestDeleteTitle.execute(this, user, chatId, new String[]{callbackQuery.getId()});
            } else if (callbackQuery.getData().contains("btn_weeks_page_")) {
                String page = callbackQuery.getData().split("btn_weeks_page_")[1];
                String messageId = String.valueOf(callbackQuery.getMessage().getMessageId());
                this.updateReminderWeeksKeyboard.execute(this, user, chatId,
                        new String[]{page, messageId, callbackQuery.getInlineMessageId()});
            } else if (callbackQuery.getData().contains("btn_week_")) {
                String week = callbackQuery.getData().split("btn_week_")[1];
                String messageId = String.valueOf(callbackQuery.getMessage().getMessageId());
                this.addWeek.execute(this, user, chatId,
                        new String[]{week, callbackQuery.getId(), messageId, callbackQuery.getInlineMessageId()});
            } else if (callbackQuery.getData().contains("btn_weeks_all")) {
                String messageId = String.valueOf(callbackQuery.getMessage().getMessageId());
                this.addAllWeeks.execute(this, user, chatId,
                        new String[]{callbackQuery.getId(), messageId, callbackQuery.getInlineMessageId()});
            } else if (callbackQuery.getData().contains("btn_weeks_done")) {
                this.requestReminderDays.execute(this, user, chatId,
                        new String[]{callbackQuery.getId()});
            } else if (callbackQuery.getData().equals("btn_list")) {
                this.showReminderList.execute(this, chatId, user,
                        new String[]{callbackQuery.getId()});
            } else if (callbackQuery.getData().contains("btn_day_of_week_")) {
                if (callbackQuery.getData().equals("btn_day_of_week_all")) {
                    this.addAllDaysOfWeek.execute(this, user, chatId, new String[]{callbackQuery.getId()});
                } else if (callbackQuery.getData().contains("btn_day_of_week_done")) {
                    this.requestReminderTime.execute(this, user, chatId,
                            new String[]{callbackQuery.getId()});
                } else {
                    String day = callbackQuery.getData().split("btn_day_of_week_")[1];
                    String messageId = String.valueOf(callbackQuery.getMessage().getMessageId());
                    this.addDayOfWeek.execute(this, user, chatId,
                            new String[]{day, callbackQuery.getId(), messageId, callbackQuery.getInlineMessageId()});
                }
            } else if (callbackQuery.getData().equals("btn_time_done")) {
                this.submitReminder.execute(this, user, chatId,
                        new String[]{callbackQuery.getId()});
            } else if (callbackQuery.getData().contains("btn_time_")) {
                logger.info("{} button pressed", callbackQuery.getData());
                String button = callbackQuery.getData();
                String messageId = String.valueOf(callbackQuery.getMessage().getMessageId());
                this.updateTime.execute(this, user, chatId,
                        new String[]{button, messageId, callbackQuery.getInlineMessageId()});
            }

        }
    }


    @Override
    public void onRegister() {
        super.onRegister();
    }

    @Override
    public String getBaseUrl() {
        return super.getBaseUrl();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ReminderPollBot.applicationContext = applicationContext;
    }

    @Scheduled(cron = "0 * * * * ?", zone = "UTC")
    public void scheduleReminders() {
        reminderService.remind(this);
    }
}
