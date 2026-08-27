package com.revende.backend.dto;

import com.revende.backend.model.ListingStatus;
import com.revende.backend.model.TicketListing;
import com.revende.backend.model.TicketType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class ListingDtos {

    public record ListingRequest(
            @NotNull Long eventId,
            @NotNull TicketType ticketType,
            @NotNull @Positive BigDecimal originalPrice,
            @NotNull @Positive BigDecimal price,
            @NotNull @Positive Integer quantity,
            String description
    ) {}

    public record ListingResponse(
            Long id,
            EventDtos.EventResponse event,
            Long sellerId,
            String sellerName,
            TicketType ticketType,
            BigDecimal originalPrice,
            BigDecimal price,
            Integer quantity,
            String description,
            ListingStatus status
    ) {
        public static ListingResponse from(TicketListing l) {
            return new ListingResponse(
                    l.getId(),
                    EventDtos.EventResponse.from(l.getEvent()),
                    l.getSeller().getId(),
                    l.getSeller().getName(),
                    l.getTicketType(),
                    l.getOriginalPrice(),
                    l.getPrice(),
                    l.getQuantity(),
                    l.getDescription(),
                    l.getStatus());
        }
    }
}
