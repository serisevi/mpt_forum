package com.example.forummpt.repo;

import com.example.forummpt.models.Specializations;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface SpecializationsRepo extends JpaRepository<Specializations, Long> {
    Specializations searchById(Long id);
    Specializations searchBySpecialization(String spec);
}
