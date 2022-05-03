package com.example.forummpt.controllers;

import com.example.forummpt.models.GlobalBanList;
import com.example.forummpt.models.Notifications;
import com.example.forummpt.models.Threads;
import com.example.forummpt.models.User;
import com.example.forummpt.repo.*;
import com.example.forummpt.services.ThreadsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ThreadsController {

    private final ThreadsService threadsService;

    @Autowired
    private ThreadsRepo threadsRepository;
    @Autowired
    private UsersRepo usersRepository;
    @Autowired
    private GlobalBanListRepo globalBanListRepository;
    @Autowired
    private NotificationsRepo notificationsRepository;
    @Autowired
    public ThreadsController(ThreadsService threadsService) {
        this.threadsService = threadsService;
    }

    String accountPicture = "https://sun9-70.userapi.com/impg/r2f8M04uAD5w5LwiKnpRFL-L_87ICNTubnM4PA/YH9jcVk9oKc.jpg?size=512x512&quality=96&sign=a5a5a1618e046adf8f4a067f70a77826&type=album";
    String noNotificationPicture = "https://sun9-8.userapi.com/impf/boLC2lY0cXiOCAc9BGL99Vpc-dH1SOd4e54hxA/is9g3PWO9GM.jpg?size=233x267&quality=96&sign=c16885d9baded99de18c7b941d9a89ba&type=album";
    String notificationPicture = "https://sun9-7.userapi.com/impf/AnRJAczQopk6loErvjJnC74Q4kW9vCRvTRqbcA/MT9oJSMvPKk.jpg?size=268x273&quality=96&sign=69342b74aa7688b6683417678ee6e541&type=album";

    public boolean checkGlobalBan(User user) {
        GlobalBanList globalBan = globalBanListRepository.searchByBannedUser(user);
        if (user.isActive() == false) { return true; }
        if (globalBan != null && globalBan.getBanExpireDate().after(new java.util.Date())) { return true; }
        else { return false; }
    }

    public boolean checkNotification(User user) {
        List<Notifications> notifications = notificationsRepository.searchByUserAndNotificationRead(user, false);
        if (notifications.size() > 0) { return true; }
        else { return false; }
    }

    @GetMapping("/main")
    public String mainPageGet(@RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
                              @RequestParam(value = "size", required = false, defaultValue = "10") int size, Model model){
        model.addAttribute("title","Главная страница");
        model.addAttribute("threads", threadsService.getPage(pageNumber, size));
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user.getUserInfo().getImageUrl() != null && user.getUserInfo().getImageUrl() != "") { model.addAttribute("imgSrc", user.getUserInfo().getImageUrl()); }
        else { model.addAttribute("imgSrc", accountPicture); }
        if (checkNotification(user) == true) { model.addAttribute("imgNtf",notificationPicture); }
        else { model.addAttribute("imgNtf", noNotificationPicture); }
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]")) { model.addAttribute("delete", "Удалить"); }
        if (checkGlobalBan(user) == false) { return "ThreadsByPage"; }
        else { model.addAttribute("message","Вы временно заблокированы"); return "Authorization"; }
    }

    @PostMapping("/main")
    public String mainPagePost(@RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
                               @RequestParam(value = "size", required = false, defaultValue = "10") int size, Model model){
        model.addAttribute("title","Главная страница");
        model.addAttribute("threads", threadsService.getPage(pageNumber, size));
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName().toString());
        if (user.getUserInfo() != null && user.getUserInfo().getImageUrl() != null && user.getUserInfo().getImageUrl() != "") { model.addAttribute("imgSrc", user.getUserInfo().getImageUrl()); }
        else { model.addAttribute("imgSrc", accountPicture); }
        if (checkNotification(user) == true) { model.addAttribute("imgNtf", notificationPicture); }
        else { model.addAttribute("imgNtf", noNotificationPicture); }
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]")) { model.addAttribute("delete", "Удалить"); }
        if (checkGlobalBan(user) == false) { return "ThreadsByPage"; }
        else { model.addAttribute("message","Вы временно заблокированы"); return "Authorization"; }
    }

    @PostMapping("/threads/add")
    public String threadAdd(String threadName, String threadDescription, Model model) {
        if (threadName != null && threadName != "" && threadDescription != null && threadDescription != "") {
            java.util.Date utilDate = new java.util.Date();
            User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
            GlobalBanList globalBan = globalBanListRepository.searchByBannedUser(user);
            java.sql.Date currentDate = new java.sql.Date(new java.util.Date().getTime());
            Threads thread = new Threads();
            thread.setThreadName(threadName);
            thread.setThreadDescription(threadDescription);
            thread.setThreadCreationTime(new java.sql.Date(utilDate.getTime()));
            thread.setThreadAuthor(user);
            if (globalBan == null) { threadsRepository.save(thread); }
            else { if (currentDate.after(globalBan.getBanExpireDate())) { threadsRepository.save(thread); } }
            int number = (int) Math.ceil((threadsRepository.findAll().indexOf(thread)+1.0)/10.0);
            return "redirect:/main?pageNumber="+String.valueOf(number);
        }
        return "redirect:/main";
    }

    @GetMapping("/threads/{id}/delete")
    public String threadDelete(@PathVariable(value = "id") long id, Model model) {
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]")) {
            threadsRepository.delete(threadsRepository.searchById(id));
        }
        return "redirect:/main";
    }

    @PostMapping("/threads/search")
    public String threadSearch(String search, Model model) {
        if (search.equals("") == true) { return "redirect:/main"; }
        java.util.Date utilDate = new java.util.Date();
        model.addAttribute("title","Главная страница");
        Iterable<Threads> threads = threadsRepository.searchByThreadDescriptionContainsOrThreadNameContains(search, search);
        model.addAttribute("threads", threads);
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]")) { model.addAttribute("delete", "Удалить"); }
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user.getUserInfo() != null && user.getUserInfo().getImageUrl() != null && user.getUserInfo().getImageUrl() != "") { model.addAttribute("imgSrc", user.getUserInfo().getImageUrl()); }
        else { model.addAttribute("imgSrc", accountPicture); }
        if (checkNotification(user) == true) { model.addAttribute("imgNtf", notificationPicture); }
        else { model.addAttribute("imgNtf", noNotificationPicture); }
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]")) { model.addAttribute("delete", "Удалить"); }
        if (checkGlobalBan(user) == false) { return "Main"; }
        else { model.addAttribute("message","Вы временно заблокированы"); return "Authorization"; }
    }

}
