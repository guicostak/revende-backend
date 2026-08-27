package com.revende.backend.repository;

import com.revende.backend.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByCityIgnoreCase(String city);
    List<Event> findByNameContainingIgnoreCase(String name);
}
