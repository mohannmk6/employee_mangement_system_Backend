package com.example.ems.controller;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

import com.example.ems.entity.Holiday;
import com.example.ems.repository.HolidayRepository;

@RestController
@RequestMapping("/api/holidays")
public class HolidayController {

    private final HolidayRepository repo;

    public HolidayController(HolidayRepository r) {
        repo = r;
    }

    @GetMapping
    public List<Holiday> all() {
        return repo.findAll().stream().sorted(Comparator.comparing(Holiday::getHolidayDate, Comparator.nullsLast(Comparator.naturalOrder()))).toList();
    }

    @GetMapping("/upcoming")
    public List<Holiday> upcoming() {
        return repo.findByHolidayDateGreaterThanEqualOrderByHolidayDateAsc(LocalDate.now());
    }

    @PostMapping
    public ResponseEntity<Holiday> create(@RequestBody Holiday h) {
        if (h.getHolidayName() == null || h.getHolidayDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "holidayName and holidayDate are required");
        
        }return ResponseEntity.status(201).body(repo.save(h));
    }

    @PutMapping("/{id}")
    public Holiday update(@PathVariable Long id, @RequestBody Holiday h) {
        Holiday x = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Holiday not found"));
        x.setHolidayName(h.getHolidayName());
        x.setHolidayDate(h.getHolidayDate());
        x.setDescription(h.getDescription());
        return repo.save(x);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Holiday not found");
        
        }repo.deleteById(id);
        return ResponseEntity.ok("Holiday deleted successfully");
    }
}
