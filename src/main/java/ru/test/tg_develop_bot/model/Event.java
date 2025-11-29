package ru.test.tg_develop_bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private String id;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private int maxParticipants;
    private int currentParticipants;
}
