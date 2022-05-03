package com.example.forummpt.repo;

import com.example.forummpt.models.ResetCodes;
import com.example.forummpt.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResetCodesRepo extends JpaRepository<ResetCodes, Long> {
    ResetCodes searchById(Long id);
    ResetCodes searchByResetCode(String code);
    ResetCodes searchByUser(User user);
    ResetCodes searchByUser_Id(Long id);
    ResetCodes searchByUserAndResetCode(User user, String code);
}
