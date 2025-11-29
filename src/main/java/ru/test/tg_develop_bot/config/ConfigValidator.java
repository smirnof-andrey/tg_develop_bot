package ru.test.tg_develop_bot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class ConfigValidator implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(ConfigValidator.class);

    @Value("${BOT_TOKEN:}")
    private String botToken;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        validateEnvironmentVariables();
    }

    private void validateEnvironmentVariables() {
        if (botToken == null || botToken.isEmpty() || botToken.equals("default_fallback_token")) {
            log.error("❌ CRITICAL: BOT_TOKEN is not set properly!");
            log.error("Please set BOT_TOKEN environment variable");
            throw new IllegalStateException("BOT_TOKEN not configured");
        }

        if (botToken.startsWith("123") || botToken.length() < 30) {
            log.warn("⚠️  BOT_TOKEN looks suspicious");
        }

        log.info("✅ Environment variables validated successfully");
    }
}