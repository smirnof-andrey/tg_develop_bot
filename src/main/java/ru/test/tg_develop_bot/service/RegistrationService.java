package ru.test.tg_develop_bot.service;

import org.springframework.stereotype.Service;
import ru.test.tg_develop_bot.model.EventRegistration;
import ru.test.tg_develop_bot.model.RegistrationStatus;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import ru.test.tg_develop_bot.model.Event;

import java.util.Map;

@Service
public class RegistrationService {
    private final Map<Long, EventRegistration> registrations = new ConcurrentHashMap<>();
    private final Map<Long, RegistrationState> userStates = new ConcurrentHashMap<>();
    private final Map<String, Event> events = new ConcurrentHashMap<>();

    public enum RegistrationState {
        IDLE,
        AWAITING_FULL_NAME,
        AWAITING_EMAIL,
        AWAITING_PHONE,
        CONFIRMING_REGISTRATION
    }

    public void setUserState(Long userId, RegistrationState state) {
        userStates.put(userId, state);
    }

    public RegistrationState getUserState(Long userId) {
        return userStates.getOrDefault(userId, RegistrationState.IDLE);
    }

    public void saveRegistration(EventRegistration registration) {
        registrations.put(registration.getUserId(), registration);
    }

    public EventRegistration getRegistration(Long userId) {
        return registrations.get(userId);
    }

    public boolean isUserRegistered(Long userId, String eventId) {
        EventRegistration reg = registrations.get(userId);
        return reg != null && reg.getEventId().equals(eventId) && reg.getStatus() == RegistrationStatus.CONFIRMED;
    }

    public void addEvent(Event event) {
        events.put(event.getId(), event);
    }

    public Event getEvent(String eventId) {
        return events.get(eventId);
    }
}
