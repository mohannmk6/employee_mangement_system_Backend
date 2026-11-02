package com.example.ems.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ems.entity.Announcement;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findAllByOrderByAnnouncementDateDesc();
}
