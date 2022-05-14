package com.example.forummpt.dto;

import com.example.forummpt.models.PersonalInformation;
import com.example.forummpt.models.Specializations;
import com.example.forummpt.models.User;
import com.example.forummpt.repo.PersonalInformationRepo;

import java.sql.Date;

public class UserAppDTO {

    private String username;
    private String email;
    private Date creationTime;
    private String firstname;
    private String middlename;
    private String lastname;
    private String description;
    private String avatar;
    private int course;
    private String specialization;

    public UserAppDTO() {
    }

    public UserAppDTO(User user) {
        PersonalInformation userInfo = user.getUserInfo();
        Specializations specialization = userInfo.getSpecialization();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.creationTime = user.getDatetime();
        this.firstname = userInfo.getFirstname();
        this.middlename = userInfo.getMiddlename();
        this.lastname = userInfo.getLastname();
        this.description = userInfo.getDescription();
        this.avatar = userInfo.getImageUrl();
        this.course = userInfo.getCourse();
        this.specialization = specialization.getSpecialization();
    }

    public UserAppDTO(String username, String email, Date datetime, String firstname, String middlename, String lastname, String description, String imageUrl, int course, String specialization) {
        this.username = username;
        this.email = email;
        this.creationTime = datetime;
        this.firstname = firstname;
        this.middlename = middlename;
        this.lastname = lastname;
        this.description = description;
        this.avatar = imageUrl;
        this.course = course;
        this.specialization = specialization;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date datetime) {
        this.creationTime = datetime;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String imageUrl) {
        this.avatar = imageUrl;
    }

    public int getCourse() {
        return course;
    }

    public void setCourse(int course) {
        this.course = course;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}
