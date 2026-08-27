package com.revende.backend.service;

import com.revende.backend.dto.EventDtos.EventRequest;
import com.revende.backend.dto.EventDtos.EventResponse;
import com.revende.backend.exception.NotFoundException;
import com.revende.backend.model.Event;
import com.revende.backend.repository.EventRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<EventResponse> list(String city, String name) {
        List<Event> events;
        if (city != null && !city.isBlank()) {
            events = eventRepository.findByCityIgnoreCase(city);
        } else if (name != null && !name.isBlank()) {
            events = eventRepository.findByNameContainingIgnoreCase(name);
        } else {
            events = eventRepository.findAll();
        }
        return events.stream().map(EventResponse::from).toList();
    }

    public EventResponse get(Long id) {
        return EventResponse.from(findEntity(id));
    }

    public Event findEntity(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new NotFoundException("Evento não encontrado: " + id));
    }

    public EventResponse create(EventRequest req) {
        Event e = new Event();
        e.setName(req.name());
        e.setDescription(req.description());
        e.setDate(req.date());
        e.setVenue(req.venue());
        e.setCity(req.city());
        e.setCategory(req.category());
        e.setImageUrl(req.imageUrl());
        return EventResponse.from(eventRepository.save(e));
    }
}
