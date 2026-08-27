package com.revende.backend.repository;

import com.revende.backend.model.Event;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByCityIgnoreCase(String city);

    List<Event> findByNameContainingIgnoreCase(String name);
}
