package com.hms.model;

import com.hms.model.enums.Gender;
import com.hms.repository.Identifiable;

import java.time.LocalDate;

/**
 * Represents a hospital patient entity.
 * Demonstrates high cohesion by encapsulating clinical demographics without unnecessary User coupling.
 */
public class Patient implements Identifiable, Searchable {
    private final String id;
    private String name;
    private int age;
    private Gender gender;
    private String phone;
    private String email;
    private String address;
    private String bloodGroup;
    private String emergencyContact;
    private final LocalDate registrationDate;
    private boolean active;

    public Patient(String id, String name, int age, Gender gender, String phone,
                   String email, String address, String bloodGroup,
                   String emergencyContact, LocalDate registrationDate, boolean active) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = (gender != null) ? gender : Gender.OTHER;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.bloodGroup = bloodGroup;
        this.emergencyContact = emergencyContact;
        this.registrationDate = (registrationDate != null) ? registrationDate : LocalDate.now();
        this.active = active;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean matches(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String q = query.toLowerCase();
        return id.toLowerCase().contains(q)
                || name.toLowerCase().contains(q)
                || (phone != null && phone.contains(q))
                || (email != null && email.toLowerCase().contains(q))
                || (bloodGroup != null && bloodGroup.toLowerCase().contains(q));
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Age: %d | Gender: %s | Phone: %s | Blood: %s | Active: %s",
                id, name, age, gender, phone, bloodGroup, active ? "Yes" : "No");
    }
}
