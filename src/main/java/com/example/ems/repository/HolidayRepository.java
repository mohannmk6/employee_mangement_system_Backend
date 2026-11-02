package com.example.ems.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ems.entity.Holiday;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    List<Holiday> findByHolidayDateGreaterThanEqualOrderByHolidayDateAsc(LocalDate date);
}
