package com.example.forummpt.dto;

import com.example.forummpt.models.PersonalInformation;

public class UserInfoDTO {

    private long id;
    private String firstname;
    private String middlename;
    private String lastname;
    private String description;
    private String imageUrl;
    private int course;
    private Long specializationId;

    public UserInfoDTO(PersonalInformation personalInfo) {
        this.id = personalInfo.getId();
        this.firstname = personalInfo.getFirstname();
        this.middlename = personalInfo.getMiddlename();
        this.lastname = personalInfo.getLastname();
        this.description = personalInfo.getDescription();
        this.imageUrl = personalInfo.getImageUrl();
        this.course = personalInfo.getCourse();
        this.specializationId = personalInfo.getSpecialization().getId();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getMiddlename() {
        return middlename;
    }

    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getCourse() {
        return course;
    }

    public void setCourse(int course) {
        this.course = course;
    }

    public Long getSpecializationId() {
        return specializationId;
    }

    public void setSpecializationId(Long specializationId) {
        this.specializationId = specializationId;
    }
}
