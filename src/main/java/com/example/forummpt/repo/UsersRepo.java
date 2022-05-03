package com.example.forummpt.repo;

import com.example.forummpt.models.Role;
import com.example.forummpt.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.sql.Timestamp;
import java.util.List;

public interface UsersRepo extends JpaRepository<User, Long> {
    User searchByUsername(String username);
    User searchByUsernameOrEmail(String username, String email);
    User searchByEmail(String email);
    User searchById(Long id);
    List<User> searchByDatetimeBetween(Timestamp date1, Timestamp date2);
    List<User> searchByRolesContains(Role role);
    List<User> searchByUsernameContainsOrEmailContainsOrPasswordContains(String text1, String text2, String text3);
}
