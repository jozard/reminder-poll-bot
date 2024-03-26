package com.jozard.reminderpollbot.actions;

import com.jozard.reminderpollbot.jobs.StateMachineCleanup;
import com.jozard.reminderpollbot.service.ChatService;
import com.jozard.reminderpollbot.service.MessageService;
import com.jozard.reminderpollbot.service.StateMachine;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledFuture;

@Component
public class AddReminder extends Action {
    private final RequestReminderTitle requestReminderTitle;
    private final ThreadPoolTaskScheduler taskScheduler;

    public AddReminder(ChatService chatService, MessageService messageService, RequestReminderTitle requestReminderTitle, ThreadPoolTaskScheduler taskScheduler) {
        super(messageService, chatService);
        this.requestReminderTitle = requestReminderTitle;
        this.taskScheduler = taskScheduler;
    }

    @Override
    protected void doExecute(AbsSender absSender, StateMachine chatState, User user, String[] arguments) {
        long chatId = chatState.getChatId();
        logger.info("User {} adds reminder to chat {}",
                user.getUserName() == null ? user.getFirstName() : user.getUserName(), chatId);
        StateMachine state = chatService.getOrCreate(chatId, user);
        logger.debug("User state is {}", state.getCurrentState());
        if (state.isNone()) {
            ScheduledFuture<?> cleanupTask = taskScheduler.schedule(
                    new StateMachineCleanup(chatId, chatService, absSender),
                    Instant.now().plus(5, ChronoUnit.MINUTES));
            state.setCleanupTask(cleanupTask);
            state.setPendingTitle();
            this.requestReminderTitle.execute(absSender, user, chatId, null);
        } else {

            sendAnswerCallbackQuery(absSender,
                    MessageFormat.format(
                            """
                                    {0}, you have already been adding/removing a reminder.
                                     Answer the last request or use the /stop command {1}""", user.getUserName(),
                            ":wink:"), arguments[0]);

        }

    }


}
