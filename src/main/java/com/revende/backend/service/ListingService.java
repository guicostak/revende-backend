package com.revende.backend.service;

import com.revende.backend.dto.ListingDtos.ListingRequest;
import com.revende.backend.dto.ListingDtos.ListingResponse;
import com.revende.backend.exception.NotFoundException;
import com.revende.backend.model.Event;
import com.revende.backend.model.ListingStatus;
import com.revende.backend.model.TicketListing;
import com.revende.backend.model.User;
import com.revende.backend.repository.TicketListingRepository;
import com.revende.backend.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListingService {

    private final TicketListingRepository listingRepository;
    private final UserRepository userRepository;
    private final EventService eventService;

    public ListingService(
            TicketListingRepository listingRepository, UserRepository userRepository, EventService eventService) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    public List<ListingResponse> listActive(Long eventId) {
        List<TicketListing> listings = (eventId != null)
                ? listingRepository.findByEventIdAndStatus(eventId, ListingStatus.ATIVO)
                : listingRepository.findByStatus(ListingStatus.ATIVO);
        return listings.stream().map(ListingResponse::from).toList();
    }

    public ListingResponse get(Long id) {
        return ListingResponse.from(findEntity(id));
    }

    public List<ListingResponse> myListings(String email) {
        User seller = requireUser(email);
        return listingRepository.findBySellerId(seller.getId()).stream()
                .map(ListingResponse::from)
                .toList();
    }

    public ListingResponse create(ListingRequest req, String sellerEmail) {
        User seller = requireUser(sellerEmail);
        Event event = eventService.findEntity(req.eventId());

        TicketListing listing = new TicketListing();
        listing.setEvent(event);
        listing.setSeller(seller);
        listing.setTicketType(req.ticketType());
        listing.setOriginalPrice(req.originalPrice());
        listing.setPrice(req.price());
        listing.setQuantity(req.quantity());
        listing.setDescription(req.description());
        listing.setStatus(ListingStatus.ATIVO);

        return ListingResponse.from(listingRepository.save(listing));
    }

    public ListingResponse markSold(Long id, String requesterEmail) {
        TicketListing listing = findEntity(id);
        ensureOwner(listing, requesterEmail);
        listing.setStatus(ListingStatus.VENDIDO);
        return ListingResponse.from(listingRepository.save(listing));
    }

    public void cancel(Long id, String requesterEmail) {
        TicketListing listing = findEntity(id);
        ensureOwner(listing, requesterEmail);
        listing.setStatus(ListingStatus.CANCELADO);
        listingRepository.save(listing);
    }

    private TicketListing findEntity(Long id) {
        return listingRepository.findById(id).orElseThrow(() -> new NotFoundException("Anúncio não encontrado: " + id));
    }

    private User requireUser(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado: " + email));
    }

    private void ensureOwner(TicketListing listing, String email) {
        if (!listing.getSeller().getEmail().equals(email)) {
            throw new IllegalArgumentException("Você não é o dono deste anúncio");
        }
    }
}
