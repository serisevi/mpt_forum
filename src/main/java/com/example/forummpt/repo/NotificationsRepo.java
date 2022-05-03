package com.example.forummpt.repo;

import com.example.forummpt.models.Messages;
import com.example.forummpt.models.Notifications;
import com.example.forummpt.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationsRepo extends JpaRepository<Notifications, Long> {
    Notifications searchById(Long id);
    List<Notifications> searchByText(String search);
    List<Notifications> searchByUser(User user);
    List<Notifications> searchByUser_Id(Long id);
    List<Notifications> searchByMessage(Messages message);
    List<Notifications> searchByUserAndNotificationRead(User user, boolean read);
}
