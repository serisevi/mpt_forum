package com.example.forummpt.models;

import com.example.forummpt.dto.UserInfoDTO;
import com.example.forummpt.repo.SpecializationsRepo;

import javax.persistence.*;
import javax.validation.constraints.*;

@Entity
public class PersonalInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String firstname;
    private String middlename;
    private String lastname;
    private String description;
    private String imageUrl;
    private int course;
    @OneToOne(optional = true)
    private Specializations specialization;

    public PersonalInformation() {
    }

    public PersonalInformation(UserInfoDTO dto, SpecializationsRepo specializationsRepo) {
        this.id = dto.getId();
        this.firstname = dto.getFirstname();
        this.middlename = dto.getMiddlename();
        this.lastname = dto.getLastname();
        this.description = dto.getDescription();
        this.imageUrl = dto.getImageUrl();
        this.course = dto.getCourse();
        this.specialization = specializationsRepo.searchById(dto.getSpecializationId());
    }

    public PersonalInformation(long id, String firstname, String middlename, String lastname, String description, String imageUrl, int course, Specializations specialization) {
        this.id = id;
        this.firstname = firstname;
        this.middlename = middlename;
        this.lastname = lastname;
        this.description = description;
        this.imageUrl = imageUrl;
        this.course = course;
        this.specialization = specialization;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Specializations getSpecialization() {
        return specialization;
    }

    public void setSpecialization(Specializations specialization) {
        this.specialization = specialization;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public int getCourse() {
        return course;
    }

    public void setCourse(int course) {
        this.course = course;
    }
}
