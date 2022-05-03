package com.example.forummpt.repo;

import com.example.forummpt.models.MessageImages;
import com.example.forummpt.models.Messages;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageImageRepo extends JpaRepository<MessageImages, Long> {
    List<MessageImages> searchByMessageOrderById(Messages message);
    MessageImages searchById(Long id);
    List<MessageImages> searchByMessage_IdOrderById(Long id);
}
