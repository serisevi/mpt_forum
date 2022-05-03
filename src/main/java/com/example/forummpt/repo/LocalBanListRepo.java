package com.example.forummpt.repo;

import com.example.forummpt.models.LocalBanList;
import com.example.forummpt.models.Threads;
import com.example.forummpt.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LocalBanListRepo extends JpaRepository<LocalBanList, Long> {
    LocalBanList searchById(long id);
    List<LocalBanList> searchByBannedUser(User user);
    List<LocalBanList> searchByBannedUser_Id(Long id);
    List<LocalBanList> searchByBanThread(Threads threads);
    List<LocalBanList> searchByBanThread_Id(Long id);
    LocalBanList searchByBanThreadAndBannedUser(Threads threads, User user);
}
