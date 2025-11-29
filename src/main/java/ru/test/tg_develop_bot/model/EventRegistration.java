package ru.test.tg_develop_bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventRegistration {
    private Long userId;
    private String userName;
    private String fullName;
    private String email;
    private String phone;
    private String eventId;
    private LocalDateTime registrationDate;
    private RegistrationStatus status;
}
