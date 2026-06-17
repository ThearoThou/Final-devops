package com.example.demo.model;

import java.time.LocalDate;

public class ProfileBuilder {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private ProfileType profileType;
    private String department;
    private String position;
    private String address;
    private LocalDate dateOfBirth;
    private String photoPath;
    
    // Private constructor for builder
    private ProfileBuilder() {
    }
    
    // Static builder method - THIS IS WHAT THE CONTROLLER IS CALLING
    public static ProfileBuilder builder() {
        return new ProfileBuilder();
    }
    
    // Builder methods (return this for chaining)
    public ProfileBuilder firstName(String firstName) {
        this.firstName = firstName;
        return this;
    }
    
    public ProfileBuilder lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }
    
    public ProfileBuilder email(String email) {
        this.email = email;
        return this;
    }
    
    public ProfileBuilder phoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }
    
    public ProfileBuilder profileType(ProfileType profileType) {
        this.profileType = profileType;
        return this;
    }
    
    public ProfileBuilder department(String department) {
        this.department = department;
        return this;
    }
    
    public ProfileBuilder position(String position) {
        this.position = position;
        return this;
    }
    
    public ProfileBuilder address(String address) {
        this.address = address;
        return this;
    }
    
    public ProfileBuilder dateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }
    
    public ProfileBuilder photoPath(String photoPath) {
        this.photoPath = photoPath;
        return this;
    }
    
    public Profile build() {
        Profile profile = new Profile();
        profile.setFirstName(this.firstName);
        profile.setLastName(this.lastName);
        profile.setEmail(this.email);
        profile.setPhoneNumber(this.phoneNumber);
        profile.setProfileType(this.profileType);
        profile.setDepartment(this.department);
        profile.setPosition(this.position);
        profile.setAddress(this.address);
        profile.setDateOfBirth(this.dateOfBirth);
        profile.setPhotoPath(this.photoPath);
        
        // Generate registration number
        String regNumber = generateRegistrationNumber();
        profile.generateUniqueId(regNumber);
        
        return profile;
    }
    
    private String generateRegistrationNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        String deptCode = department != null ? department.substring(0, Math.min(3, department.length())).toUpperCase() : "GEN";
        String random = String.format("%04d", (int)(Math.random() * 10000));
        return year + "-" + deptCode + "-" + random;
    }
}