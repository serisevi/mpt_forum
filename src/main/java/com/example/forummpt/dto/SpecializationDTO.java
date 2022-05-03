package com.example.forummpt.dto;

import com.example.forummpt.models.Specializations;

public class SpecializationDTO {

    private long id;
    private String specialization;

    public SpecializationDTO(Specializations specialization) {
        this.id = specialization.getId();
        this.specialization = specialization.getSpecialization();
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
