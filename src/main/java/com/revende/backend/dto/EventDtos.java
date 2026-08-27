package com.revende.backend.dto;

import com.revende.backend.model.Event;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class EventDtos {

    public record EventRequest(
            @NotBlank String name,
            String description,
            @NotNull LocalDateTime date,
            @NotBlank String venue,
            @NotBlank String city,
            String category,
            String imageUrl
    ) {}

    public record EventResponse(
            Long id,
            String name,
            String description,
            LocalDateTime date,
            String venue,
            String city,
            String category,
            String imageUrl
    ) {
        public static EventResponse from(Event e) {
            return new EventResponse(
                    e.getId(), e.getName(), e.getDescription(), e.getDate(),
                    e.getVenue(), e.getCity(), e.getCategory(), e.getImageUrl());
        }
    }
}
