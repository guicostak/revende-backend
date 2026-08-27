package com.revende.backend.controller;

import com.revende.backend.dto.ListingDtos.ListingRequest;
import com.revende.backend.dto.ListingDtos.ListingResponse;
import com.revende.backend.service.ListingService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    @GetMapping
    public List<ListingResponse> list(@RequestParam(required = false) Long eventId) {
        return listingService.listActive(eventId);
    }

    @GetMapping("/{id}")
    public ListingResponse get(@PathVariable Long id) {
        return listingService.get(id);
    }

    @GetMapping("/me")
    public List<ListingResponse> myListings(Authentication auth) {
        return listingService.myListings(auth.getName());
    }

    @PostMapping
    public ResponseEntity<ListingResponse> create(@Valid @RequestBody ListingRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listingService.create(req, auth.getName()));
    }

    @PatchMapping("/{id}/sold")
    public ListingResponse markSold(@PathVariable Long id, Authentication auth) {
        return listingService.markSold(id, auth.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id, Authentication auth) {
        listingService.cancel(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
