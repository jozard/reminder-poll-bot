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
public class RequestDeleteTitle extends Action {

    private final ThreadPoolTaskScheduler taskScheduler;

    public RequestDeleteTitle(ChatService chatService, MessageService messageService, ThreadPoolTaskScheduler taskScheduler) {
        super(messageService, chatService);
        this.taskScheduler = taskScheduler;
    }

    @Override
    protected void doExecute(AbsSender absSender, StateMachine state, User user, String[] arguments) {
        long chatId = state.getChatId();
        logger.debug("User state is {}", state.getCurrentState());
        if (state.isNone()) {
            state.setPendingDeleteTitle();
            ScheduledFuture<?> cleanupTask = taskScheduler.schedule(
                    new StateMachineCleanup(chatId, chatService, absSender),
                    Instant.now().plus(5, ChronoUnit.MINUTES));
            state.setCleanupTask(cleanupTask);
            messageService.send(absSender, chatId,
                    """
                            Reply to this message with a poll title to be removed""");
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
