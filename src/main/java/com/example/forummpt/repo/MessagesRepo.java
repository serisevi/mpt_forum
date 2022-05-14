package com.example.forummpt.repo;

import com.example.forummpt.models.Messages;
import com.example.forummpt.models.Threads;
import com.example.forummpt.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.sql.Timestamp;
import java.util.List;

public interface MessagesRepo extends JpaRepository<Messages, Long>{
    Messages searchById(Long id);
    List<Messages> searchByThread(Threads thread);
    List<Messages> searchByThreadAndMessageTextContains(Threads thread, String text);
    List<Messages> searchByThread_IdAndMessageTextContainsOrMessageAuthor_Username(Long id, String text1, String text2);
    List<Messages> searchByMessageDatetimeBetween(Timestamp date1, Timestamp date2);
    List<Messages> searchByMessageDatetimeBetweenAndMessageAuthor(Timestamp date1, Timestamp date2, User user);
    List<Messages> searchByMessageTextContains(String text1);
    List<Messages> searchByMessageAuthor(User user);
    List<Messages> searchByThread_Id(Long id);
    List<Messages> searchByMessageAuthor_Id(Long id);
    Page<Messages> findMessagesByThread(Threads thread, Pageable pageable);
}
