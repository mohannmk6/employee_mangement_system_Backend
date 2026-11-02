package com.example.ems.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true)
    private String email;
    private String role;
    private String department;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 255)
    private String password;
    // Optional profile fields used by the Angular UI; Hibernate may add these columns safely.
    private String phone;
    private String location;
    private String website;
    @Column(length = 1000)
    private String bio;
    @Transient
    private String username;
@Column(name = "active")
private Boolean active = true;

public Boolean getActive() {
    return active;
}

public void setActive(Boolean active) {
    this.active = active;
}
    public Employee() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long v) {
        id = v;
    }

    public String getName() {
        return name;
    }

    public void setName(String v) {
        name = v;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String v) {
        email = v;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String v) {
        role = v;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String v) {
        department = v;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String v) {
        password = v;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String v) {
        phone = v;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String v) {
        location = v;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String v) {
        website = v;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String v) {
        bio = v;
    }

    public String getUsername() {
        return username == null ? email : username;
    }

    public void setUsername(String v) {
        username = v;
    }
}
