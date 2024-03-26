package com.jozard.reminderpollbot.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

@Service
public class ChatService {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    // chat_id -> group
    private final Map<Long, StateMachine> chats = new HashMap<>();
    private final Map<Long, ScheduledFuture<Long>> cleanupTasks = new HashMap<>();

    public ChatService() {
    }

    public final StateMachine getOrCreate(long chatId, User user) {
        // if no such instance, create one with the user
        StateMachine item = chats.getOrDefault(chatId, new StateMachine(user, chatId));
        this.chats.put(chatId, item);
        return item;
    }

    public final void remove(long chatId) {
        logger.info("Remove state machine for chat ID = " + chatId);
        chats.get(chatId).getCleanupTask().cancel(true);
        chats.remove(chatId);
    }

    public boolean chatExists(long chatId) {
        return chats.containsKey(chatId);
    }

    public Optional<StateMachine> getChatState(Long chatId) {
        return Optional.ofNullable(chats.get(chatId));
    }

}
