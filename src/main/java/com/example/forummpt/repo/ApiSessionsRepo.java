package com.example.forummpt.repo;

import com.example.forummpt.models.ApiSessions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiSessionsRepo extends JpaRepository<ApiSessions, Long> {
    ApiSessions searchById(Long id);
    ApiSessions searchByUser_Id(Long id);
    ApiSessions searchByToken(String token);
}
