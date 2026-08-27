package com.revende.backend.repository;

import com.revende.backend.model.ListingStatus;
import com.revende.backend.model.TicketListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketListingRepository extends JpaRepository<TicketListing, Long> {
    List<TicketListing> findByStatus(ListingStatus status);
    List<TicketListing> findByEventIdAndStatus(Long eventId, ListingStatus status);
    List<TicketListing> findBySellerId(Long sellerId);
}
