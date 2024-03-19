package com.jozard.reminderpollbot.users;


import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ChatService {
    // chat_id -> group
    private final Map<Long, ChatInstance> chats = new HashMap<>();

    public final ChatInstance get(long chatId, User admin) {
        // if no such instance, create one with the admin user
        ChatInstance item = chats.getOrDefault(chatId, new ChatInstance(chatId, admin));
        this.chats.put(chatId, item);
        return item;
    }

    public final void remove(long chatId) {
        chats.remove(chatId);
    }

    public boolean chatExists(long chatId) {
        return chats.containsKey(chatId);
    }

    public Optional<ChatInstance> getChat(Long id) {
        return Optional.ofNullable(chats.get(id));
    }

    public class ChatInstance {
        private final long chatId;
        private final User botAdmin;
        private StateMachine state;

        public ChatInstance(long chatId, User botAdmin) {
            this.chatId = chatId;
            this.botAdmin = botAdmin;
        }

        public User getAdmin() {
            return botAdmin;
        }

        public long getChatId() {
            return chatId;
        }

        public final Optional<StateMachine> getStateMachine() {
            return Optional.ofNullable(this.state);
        }

        public final StateMachine start(User user) {
            this.state = new StateMachine(user, this);
            return this.state;
        }

    }
}
