package com.example.forummpt.models;

import com.example.forummpt.dto.SpecializationDTO;

import javax.persistence.*;

@Entity
public class Specializations {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String specialization;

    public Specializations() {
    }

    public Specializations(SpecializationDTO dto) {
        this.id = dto.getId();
        this.specialization = dto.getSpecialization();
    }

    public Specializations(long id, String specialization) {
        this.id = id;
        this.specialization = specialization;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
}
