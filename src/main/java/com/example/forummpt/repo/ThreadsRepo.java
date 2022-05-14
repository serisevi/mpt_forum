package com.example.forummpt.repo;

import com.example.forummpt.models.Threads;
import com.example.forummpt.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.sql.Timestamp;
import java.util.List;

public interface ThreadsRepo extends JpaRepository<Threads, Long> {
    Threads searchById(Long id);
    List<Threads> searchByThreadDescriptionContainsOrThreadNameContains(String description, String name);
    List<Threads> searchByThreadCreationTimeBetween(Timestamp date1, Timestamp date2);
    List<Threads> searchByThreadCreationTimeBetweenAndThreadAuthor(Timestamp date1, Timestamp date2, User user);
    List<Threads> searchByThreadNameContainsOrThreadDescriptionContainsOrThreadAuthor(String text1, String text2, User user);
    List<Threads> searchByThreadAuthor_UsernameOrThreadNameContainsOrThreadDescriptionContains(String text1, String text2, String text3);
    List<Threads> searchByThreadAuthor(User user);
    List<Threads> searchByThreadAuthor_Id(Long id);
}
