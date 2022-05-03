package com.example.forummpt.repo;

import com.example.forummpt.models.GlobalBanList;
import com.example.forummpt.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.sql.Timestamp;
import java.util.List;

public interface GlobalBanListRepo extends JpaRepository<GlobalBanList, Long> {
    GlobalBanList searchById(long id);
    GlobalBanList searchByBannedUser(User user);
    GlobalBanList searchByBannedUser_Id(Long id);
    List<GlobalBanList> searchByBanStartDateBetween(Timestamp date1, Timestamp date2);
}
