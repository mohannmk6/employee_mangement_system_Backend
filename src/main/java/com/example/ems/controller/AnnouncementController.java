package com.example.ems.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.ems.entity.Announcement;
import com.example.ems.repository.AnnouncementRepository;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementRepository repo;

    public AnnouncementController(AnnouncementRepository r) {
        repo = r;
    }

    @GetMapping
    public List<Announcement> all() {
        return repo.findAllByOrderByAnnouncementDateDesc();
    }

    @PostMapping
    public ResponseEntity<Announcement> create(@RequestBody Announcement announcement) {
        if (announcement.getAnnouncementDate() == null) {
            announcement.setAnnouncementDate(LocalDate.now());
        
        }if (announcement.getTitle() == null || announcement.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        
        }return ResponseEntity.status(201).body(repo.save(announcement));
    }

    @PutMapping("/{id}")
    public Announcement update(@PathVariable Long id, @RequestBody Announcement announcement) {
        Announcement x = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Announcement not found"));
        x.setTitle(announcement.getTitle());
        x.setAnnouncementDate(announcement.getAnnouncementDate());
        x.setDescription(announcement.getDescription());
        return repo.save(x);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Announcement not found");
        
        }repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
