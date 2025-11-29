package ru.test.tg_develop_bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.test.tg_develop_bot.model.Event;
import ru.test.tg_develop_bot.model.EventRegistration;
import ru.test.tg_develop_bot.model.RegistrationStatus;
import ru.test.tg_develop_bot.service.RegistrationService;

import java.time.LocalDateTime;

@Component
public class EventBot extends TelegramLongPollingBot {
    @Autowired
    private RegistrationService registrationService;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            String text = message.getText();

            handleMessage(chatId, text, message.getFrom().getUserName());
        }
    }

    private void handleMessage(Long chatId, String text, String userName) {
        RegistrationService.RegistrationState state = registrationService.getUserState(chatId);

        switch (state) {
            case IDLE:
                handleIdleState(chatId, text, userName);
                break;
            case AWAITING_FULL_NAME:
                handleFullNameInput(chatId, text, userName);
                break;
            case AWAITING_EMAIL:
                handleEmailInput(chatId, text);
                break;
            case AWAITING_PHONE:
                handlePhoneInput(chatId, text);
                break;
            case CONFIRMING_REGISTRATION:
                handleConfirmation(chatId, text);
                break;
        }
    }

    private void handleIdleState(Long chatId, String text, String userName) {
        if ("/start".equals(text)) {
            sendMessage(chatId, "Добро пожаловать! Используйте /events для просмотра мероприятий");
        } else if ("/events".equals(text)) {
            showEvents(chatId);
        } else if (text.startsWith("/register_")) {
            String eventId = text.substring(10);
            startRegistration(chatId, eventId, userName);
        } else {
            sendMessage(chatId, "Используйте /events для просмотра мероприятий");
        }
    }

    private void showEvents(Long chatId) {
        // Здесь можно получить события из базы данных
        Event event1 = new Event("event1", "Конференция по Java",
                "Ежегодная конференция Java разработчиков",
                LocalDateTime.now().plusDays(7), 100, 45);

        Event event2 = new Event("event2", "Воркшоп по Spring Boot",
                "Практический воркшоп по Spring Boot",
                LocalDateTime.now().plusDays(14), 50, 25);

        registrationService.addEvent(event1);
        registrationService.addEvent(event2);

        StringBuilder sb = new StringBuilder();
        sb.append("📅 Доступные мероприятия:\n\n");

        sb.append("1. ").append(event1.getTitle()).append("\n");
        sb.append("📝 ").append(event1.getDescription()).append("\n");
        sb.append("📅 ").append(event1.getEventDate()).append("\n");
        sb.append("👥 ").append(event1.getCurrentParticipants()).append("/").append(event1.getMaxParticipants()).append("\n");
        sb.append("Зарегистрироваться: /register_event1\n\n");

        sb.append("2. ").append(event2.getTitle()).append("\n");
        sb.append("📝 ").append(event2.getDescription()).append("\n");
        sb.append("📅 ").append(event2.getEventDate()).append("\n");
        sb.append("👥 ").append(event2.getCurrentParticipants()).append("/").append(event2.getMaxParticipants()).append("\n");
        sb.append("Зарегистрироваться: /register_event2");

        sendMessage(chatId, sb.toString());
    }

    private void startRegistration(Long chatId, String eventId, String userName) {
        Event event = registrationService.getEvent(eventId);
        if (event == null) {
            sendMessage(chatId, "Мероприятие не найдено");
            return;
        }

        if (registrationService.isUserRegistered(chatId, eventId)) {
            sendMessage(chatId, "Вы уже зарегистрированы на это мероприятие!");
            return;
        }

        if (event.getCurrentParticipants() >= event.getMaxParticipants()) {
            sendMessage(chatId, "К сожалению, все места заняты!");
            return;
        }

        // Начинаем процесс регистрации
        EventRegistration registration = new EventRegistration();
        registration.setUserId(chatId);
        registration.setUserName(userName);
        registration.setEventId(eventId);
        registration.setRegistrationDate(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.PENDING);

        registrationService.saveRegistration(registration);
        registrationService.setUserState(chatId, RegistrationService.RegistrationState.AWAITING_FULL_NAME);

        sendMessage(chatId, "Начинаем регистрацию на: " + event.getTitle() +
                "\n\nПожалуйста, введите ваше полное имя:");
    }

    private void handleFullNameInput(Long chatId, String fullName, String userName) {
        EventRegistration registration = registrationService.getRegistration(chatId);
        registration.setFullName(fullName);
        registrationService.saveRegistration(registration);
        registrationService.setUserState(chatId, RegistrationService.RegistrationState.AWAITING_EMAIL);

        sendMessage(chatId, "Отлично! Теперь введите ваш email:");
    }

    private void handleEmailInput(Long chatId, String email) {
        if (!isValidEmail(email)) {
            sendMessage(chatId, "Пожалуйста, введите корректный email:");
            return;
        }

        EventRegistration registration = registrationService.getRegistration(chatId);
        registration.setEmail(email);
        registrationService.saveRegistration(registration);
        registrationService.setUserState(chatId, RegistrationService.RegistrationState.AWAITING_PHONE);

        sendMessage(chatId, "Принято! Теперь введите ваш номер телефона:");
    }

    private void handlePhoneInput(Long chatId, String phone) {
        EventRegistration registration = registrationService.getRegistration(chatId);
        registration.setPhone(phone);
        registrationService.saveRegistration(registration);
        registrationService.setUserState(chatId, RegistrationService.RegistrationState.CONFIRMING_REGISTRATION);

        // Показываем сводку для подтверждения
        Event event = registrationService.getEvent(registration.getEventId());

        String summary = "📋 Проверьте ваши данные:\n\n" +
                "👤 Имя: " + registration.getFullName() + "\n" +
                "📧 Email: " + registration.getEmail() + "\n" +
                "📞 Телефон: " + registration.getPhone() + "\n" +
                "🎯 Мероприятие: " + event.getTitle() + "\n\n" +
                "Всё верно? (да/нет)";

        sendMessage(chatId, summary);
    }

    private void handleConfirmation(Long chatId, String response) {
        if ("да".equalsIgnoreCase(response) || "yes".equalsIgnoreCase(response)) {
            // Завершаем регистрацию
            EventRegistration registration = registrationService.getRegistration(chatId);
            registration.setStatus(RegistrationStatus.CONFIRMED);
            registrationService.saveRegistration(registration);

            // Обновляем количество участников
            Event event = registrationService.getEvent(registration.getEventId());
            event.setCurrentParticipants(event.getCurrentParticipants() + 1);

            registrationService.setUserState(chatId, RegistrationService.RegistrationState.IDLE);

            sendMessage(chatId, "✅ Регистрация завершена! Ждем вас на мероприятии!\n\n" +
                    "Напоминание придет за день до события.");
        } else {
            // Отменяем регистрацию
            registrationService.setUserState(chatId, RegistrationService.RegistrationState.IDLE);
            sendMessage(chatId, "Регистрация отменена. Если передумаете - начните заново!");
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "YourBotName";
    }

    @Override
    public String getBotToken() {
        return "YOUR_BOT_TOKEN";
    }
}
