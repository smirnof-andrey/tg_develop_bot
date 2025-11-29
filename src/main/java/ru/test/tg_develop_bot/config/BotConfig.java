package ru.test.tg_develop_bot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.test.tg_develop_bot.EventBot;

@Configuration
public class BotConfig {
    private final String botToken;

    public BotConfig(@Value("${BOT_TOKEN}") String botToken) {
        if (botToken == null || botToken.equals("default_fallback_token")) {
            throw new IllegalStateException("BOT_TOKEN not configured properly");
        }
        this.botToken = botToken;
    }

    public String getBotToken() {
        return botToken;
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(EventBot eventBot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(eventBot);
        return api;
    }

}
