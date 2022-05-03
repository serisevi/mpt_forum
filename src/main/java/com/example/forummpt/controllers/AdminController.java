package com.example.forummpt.controllers;

import com.example.forummpt.models.*;
import com.example.forummpt.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAnyAuthority('ADMIN')")
public class AdminController {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ThreadsRepo threadsRepository;
    @Autowired
    private UsersRepo usersRepository;
    @Autowired
    private MessagesRepo messagesRepository;
    @Autowired
    private MessageImageRepo messageImagesRepository;
    @Autowired
    private PersonalInformationRepo personalInformationRepository;
    @Autowired
    private SpecializationsRepo specializationsRepository;
    @Autowired
    private LocalBanListRepo localBanListRepository;
    @Autowired
    private GlobalBanListRepo globalBanListRepository;
    @Autowired
    private  MessageImageRepo messageImageRepository;
    @Autowired
    private NotificationsRepo notificationsRepository;

    @GetMapping("")
    public String adminGet(Model model){
        model.addAttribute("title","Админ-панель");
        Calendar cal = Calendar.getInstance();
        Timestamp currentDate = new Timestamp(cal.getTimeInMillis());
        cal.add(Calendar.DAY_OF_MONTH, -27);
        Timestamp previousDate = new Timestamp(cal.getTimeInMillis());
        List<Messages> messagesMonthly = messagesRepository.searchByMessageDatetimeBetween(previousDate, currentDate);
        List<Threads> threadsMonthly = threadsRepository.searchByThreadCreationTimeBetween(previousDate, currentDate);
        List<GlobalBanList> globalBansMonthly = globalBanListRepository.searchByBanStartDateBetween(previousDate, currentDate);
        List<User> usersMonthly = usersRepository.searchByDatetimeBetween(previousDate, currentDate);
        List<Messages> messagesTotal = messagesRepository.findAll();
        List<Threads> threadsTotal = threadsRepository.findAll();
        List<GlobalBanList> globalBansTotal = globalBanListRepository.findAll();
        List<User> usersTotal = usersRepository.findAll();
        model.addAttribute("messagesTotal", messagesTotal.size());
        model.addAttribute("messagesMonthly", messagesMonthly.size());
        model.addAttribute("threadsTotal", threadsTotal.size());
        model.addAttribute("threadsMonthly", threadsMonthly.size());
        model.addAttribute("globalBansTotal", globalBansTotal.size());
        model.addAttribute("globalBansMonthly", globalBansMonthly.size());
        model.addAttribute("registeredUsersTotal", usersTotal.size());
        model.addAttribute("registeredUsersMonthly", usersMonthly.size());
        return "Admin";
    }

    @GetMapping("/users")
    public String adminUsers(Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("users", usersRepository.findAll());
        return "AdminUsers";
    }

    @PostMapping("/users/searchByID")
    public String adminUsersSearchById(Integer search, Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("users", usersRepository.searchById(Long.valueOf(search)));
        return "AdminUsers";
    }

    @PostMapping("/users/searchByText")
    public String adminUsersSearchById(String search, Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("users", usersRepository.searchByUsernameContainsOrEmailContainsOrPasswordContains(search, search, search));
        return "AdminUsers";
    }

    @GetMapping("/users/delete/{id}")
    public String adminUsersDelete(@PathVariable (value = "id") long id, Model model) {
        usersRepository.delete(usersRepository.searchById(id));
        return "redirect:/admin/users";
    }

    @GetMapping("/users-roles")
    public String adminUsersRoles(Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("usersRoles", usersRepository.findAll());
        return "AdminUsersRoles";
    }

    @PostMapping("/users-roles/searchByRole")
    public String adminUsersRolesSearchByRole(String search, Model model) {
        model.addAttribute("title","Админ-панель");
        if (Objects.equals(search, "ADMIN") | Objects.equals(search, "admin")) { model.addAttribute("usersRoles", usersRepository.searchByRolesContains(Role.ADMIN)); }
        if (Objects.equals(search, "USER") | Objects.equals(search, "user")) { model.addAttribute("usersRoles", usersRepository.searchByRolesContains(Role.USER)); }
        return "AdminUsersRoles";
    }

    @PostMapping("/users-roles/searchByUser")
    public String adminUsersRolesSearchByUser(Integer search, Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("usersRoles", usersRepository.searchById(Long.valueOf(search)));
        return "AdminUsersRoles";
    }

    @GetMapping("/users-info")
    public String adminUsersInfo(Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("usersInfo", personalInformationRepository.findAll());
        return "AdminUsersInfo";
    }

    @PostMapping("/users-info/searchFIO")
    public String adminUsersInfoSearchFIO(String search, Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("usersInfo", personalInformationRepository.searchByLastnameContainsOrFirstnameContainsOrMiddlenameContainsOrDescriptionContainsOrImageUrlContains(search, search, search, search, search));
        return "AdminUsersInfo";
    }

    @PostMapping("/users-info/searchCourse")
    public String adminUsersInfoSearchCourse(String search, Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("usersInfo", personalInformationRepository.searchByCourse(Integer.valueOf(search)));
        return "AdminUsersInfo";
    }

    @PostMapping("/users-info/searchSpec")
    public String adminUsersInfoSearchSpec(String search, Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("usersInfo", personalInformationRepository.searchBySpecialization(specializationsRepository.searchById(Long.valueOf(search))));
        return "AdminUsersInfo";
    }

    @GetMapping("/users-info/delete{id}")
    public String adminUsersInfoDelete(@PathVariable (value = "id") long id, Model model) {
        personalInformationRepository.delete(personalInformationRepository.searchById(id));
        return "redirect:/admin/users-info";
    }

    @PostMapping("/users-info/change/{id}")
    public String adminUserInfoChange(@PathVariable (value = "id") long id, String lastname, String firstname, String middlename, int course, long specialization, String description, String imageUrl, Model model) {
        if (lastname != "" && firstname != "" && middlename != "" && description != "" && imageUrl != "" && specializationsRepository.searchById(specialization) != null) {
            PersonalInformation userInfo = personalInformationRepository.searchById(id);
            userInfo.setLastname(lastname);
            userInfo.setFirstname(firstname);
            userInfo.setMiddlename(middlename);
            userInfo.setCourse(course);
            userInfo.setSpecialization(specializationsRepository.searchById(specialization));
            userInfo.setDescription(description);
            userInfo.setImageUrl(imageUrl);
            personalInformationRepository.save(userInfo);
        }
        return "redirect:/admin/users-info";
    }

    @PostMapping("/users-info/add")
    public String adminUserInfoAdd(String lastname, String firstname, String middlename, int course, long specialization, String description, String imageUrl, Model model) {
        if (lastname != "" && firstname != "" && middlename != "" && description != "" && imageUrl != "" && specializationsRepository.searchById(specialization) != null) {
            PersonalInformation userInfo = new PersonalInformation();
            userInfo.setLastname(lastname);
            userInfo.setFirstname(firstname);
            userInfo.setMiddlename(middlename);
            userInfo.setCourse(course);
            userInfo.setSpecialization(specializationsRepository.searchById(specialization));
            userInfo.setDescription(description);
            userInfo.setImageUrl(imageUrl);
            personalInformationRepository.save(userInfo);
        }
        return "redirect:/admin/users-info";
    }

    @GetMapping("/users-specializations")
    public String adminSpecializations(Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("specializations", specializationsRepository.findAll());
        return "AdminSpecializations";
    }

    @PostMapping("/users-specializations/search")
    public String adminSpecializationsSearch(String search, Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("specializations", specializationsRepository.searchBySpecialization(search));
        return "AdminSpecializations";
    }

    @GetMapping("/users-specializations/delete/{id}")
    public String adminSpecializationsDelete(@PathVariable (value = "id") long id, Model model) {
        specializationsRepository.delete(specializationsRepository.searchById(id));
        return "redirect:/admin/users-specializations";
    }

    @PostMapping("/users-specializations/change/{id}")
    public String adminSpecializationsChange(@PathVariable (value = "id") long id, String specialization, Model model) {
        if (specialization != "") {
            Specializations spec = specializationsRepository.searchById(id);
            spec.setSpecialization(specialization);
            specializationsRepository.save(spec);
        }
        return "redirect:/admin/users-specializations";
    }

    @PostMapping("/users-specializations/add")
    public String adminSpecializationsChange(String specialization, Model model) {
        if (specialization != "") {
            Specializations spec = new Specializations();
            spec.setSpecialization(specialization);
            specializationsRepository.save(spec);
        }
        return "redirect:/admin/users-specializations";
    }

    @GetMapping("/threads")
    public String adminThreads(Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("threads", threadsRepository.findAll());
        return "AdminThreads";
    }

    @PostMapping("/threads/searchByText")
    public String adminThreadsSearchByText(String search, Model model) {
        model.addAttribute("title","Админ-панель");
        User user = usersRepository.searchByUsername(search);
        model.addAttribute("threads", threadsRepository.searchByThreadNameContainsOrThreadDescriptionContainsOrThreadAuthor(search, search, user));
        return "AdminThreads";
    }

    @PostMapping("/threads/searchByUser")
    public String adminThreadsSearchByUser(Integer search, Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("threads", threadsRepository.searchByThreadAuthor(usersRepository.searchById(Long.valueOf(search))));
        return "AdminThreads";
    }

    @GetMapping("/threads/delete/{id}")
    public String adminThreadsDelete(@PathVariable (value = "id") long id, Model model) {
        threadsRepository.delete(threadsRepository.searchById(id));
        return "redirect:/admin/threads";
    }

    @PostMapping("/threads/change/{id}")
    public String adminThreadsChange(@PathVariable (value = "id") long id, String threadName, String threadDescription, long threadAuthor, Date threadCreationTime, Model model) {
        if (threadName != "" && threadDescription != "" && usersRepository.searchById(threadAuthor) != null) {
            Threads thread = threadsRepository.searchById(id);
            thread.setThreadName(threadName);
            thread.setThreadDescription(threadDescription);
            thread.setThreadCreationTime(threadCreationTime);
            thread.setThreadAuthor(usersRepository.searchById(threadAuthor));
            threadsRepository.save(thread);
        }
        return "redirect:/admin/threads";
    }

    @PostMapping("/threads/add")
    public String adminThreadsAdd(String threadName, String threadDescription, long threadAuthor, Date threadCreationTime, Model model) {
        if (threadName != "" && threadDescription != "" && usersRepository.searchById(threadAuthor) != null) {
            Threads thread = new Threads();
            thread.setThreadName(threadName);
            thread.setThreadDescription(threadDescription);
            thread.setThreadCreationTime(threadCreationTime);
            thread.setThreadAuthor(usersRepository.searchById(threadAuthor));
            threadsRepository.save(thread);
        }
        return "redirect:/admin/threads";
    }

    @GetMapping("/threads-messages")
    public String adminMessages(Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("messages", messagesRepository.findAll());
        return "AdminMessages";
    }

    @PostMapping("/threads-messages/searchByText")
    public String adminMessagesSearchByText(String search, Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("messages", messagesRepository.searchByMessageTextContains(search));
        return "AdminMessages";
    }

    @PostMapping("/threads-messages/searchByUser")
    public String adminMessagesSearchByUser(Integer search, Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("messages", messagesRepository.searchByMessageAuthor(usersRepository.searchById(Long.valueOf(search))));
        return "AdminMessages";
    }

    @PostMapping("/threads-messages/searchByThread")
    public String adminMessagesSearchByThread(Integer search, Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("messages", messagesRepository.searchByThread(threadsRepository.searchById(Long.valueOf(search))));
        return "AdminMessages";
    }

    @GetMapping("/threads-messages/delete/{id}")
    public String adminMessagesDelete(@PathVariable (value = "id") long id, Model model) {
        messagesRepository.delete(messagesRepository.searchById(id));
        return "redirect:/admin/threads-messages";
    }

    @PostMapping("/threads-messages/add")
    public String adminMessageAdd(String messageText, String messageDatetime, long messageAuthor, long messageThread, Long messageReply, Model model) {
        if (messageText != "" && usersRepository.searchById(messageAuthor) != null && threadsRepository.searchById(messageThread) != null) {
            Messages message = new Messages();
            message.setMessageText(messageText);
            message.setMessageDatetime(Timestamp.valueOf(messageDatetime));
            message.setMessageAuthor(usersRepository.searchById(messageAuthor));
            message.setThread(threadsRepository.searchById(messageThread));
            if (messageReply == null) { message.setMessageReply(null); }
            else { message.setMessageReply(messagesRepository.searchById(messageReply)); }
            messagesRepository.save(message);
        }
        return "redirect:/admin/threads-messages";
    }

    @PostMapping("/threads-messages/change/{id}")
    public String adminMessageChange(@PathVariable (value = "id") long id, String messageText, String messageDatetime, long messageAuthor, long messageThread, Long messageReply, Model model) {
        if (messageText != "" && usersRepository.searchById(messageAuthor) != null && threadsRepository.searchById(messageThread) != null) {
            Messages message = messagesRepository.searchById(id);
            message.setMessageText(messageText);
            message.setMessageDatetime(Timestamp.valueOf(messageDatetime));
            message.setMessageAuthor(usersRepository.searchById(messageAuthor));
            message.setThread(threadsRepository.searchById(messageThread));
            if (messageReply == null) { message.setMessageReply(null); }
            else { message.setMessageReply(messagesRepository.searchById(messageReply)); }
            messagesRepository.save(message);
        }
        return "redirect:/admin/threads-messages";
    }

    @GetMapping("/threads-messages-images")
    public String adminMessagesImagesGet(Model model) {
        model.addAttribute("title", "Админ-панель");
        model.addAttribute("messagesImages", messageImagesRepository.findAll());
        return "AdminMessagesImages";
    }

    @PostMapping("/threads-messages-images/search")
    public String adminMessagesImagesSearch(Long search, Model model) {
        model.addAttribute("title", "Админ-панель");
        model.addAttribute("messagesImages", messageImagesRepository.searchByMessageOrderById(messagesRepository.searchById(search)));
        return "AdminMessagesImages";
    }

    @GetMapping("/threads-messages-images/delete/{id}")
    public String adminMessagesImagesDelete(@PathVariable (value = "id") long id, Model model) {
        messageImagesRepository.delete(messageImagesRepository.searchById(id));
        return "redirect:/admin/threads-messages-images";
    }

    @PostMapping("/threads-messages-images/add")
    public String adminMessagesImagesAdd(String imageUrl, Long message, Model model) {
        if (imageUrl != null && imageUrl != "" && message != null) {
            MessageImages messageImages = new MessageImages();
            messageImages.setImageUrl(imageUrl);
            messageImages.setMessage(messagesRepository.searchById(message));
            messageImagesRepository.save(messageImages);
        }
        return "redirect:/admin/threads-messages-images";
    }

    @PostMapping("/threads-messages-images/change/{id}")
    public String adminMessagesImagesChange(@PathVariable (value = "id") long id, String imageUrl, Long message, Model model) {
        if (imageUrl != null && imageUrl != "" && message != null) {
            MessageImages messageImages = messageImagesRepository.searchById(id);
            messageImages.setImageUrl(imageUrl);
            messageImages.setMessage(messagesRepository.searchById(message));
            messageImagesRepository.save(messageImages);
        }
        return "redirect:/admin/threads-messages-images";
    }

    @GetMapping("/local-bans")
    public String adminLocalBans(Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("localbans", localBanListRepository.findAll());
        return "AdminLocalBans";
    }

    @PostMapping("/local-bans/search")
    public String adminLocalBansSearch(String search, Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("localbans", localBanListRepository.searchByBanThread(threadsRepository.searchById(Long.valueOf(search))));
        return "AdminLocalBans";
    }

    @GetMapping("/local-bans/delete/{id}")
    public String adminLocalBansDelete(@PathVariable (value = "id") long id, Model model) {
        localBanListRepository.delete(localBanListRepository.searchById(id));
        return "redirect:/admin/local-bans";
    }

    @PostMapping("/local-bans/change/{id}")
    public String adminLocalBansChange(@PathVariable (value = "id") long id, long bannedUser, long banThread, Model model) {
        if (usersRepository.searchById(bannedUser) != null && threadsRepository.searchById(banThread) != null) {
            LocalBanList localBan = localBanListRepository.searchById(id);
            localBan.setBannedUser(usersRepository.searchById(bannedUser));
            localBan.setBanThread(threadsRepository.searchById(banThread));
            localBanListRepository.save(localBan);
        }
        return "redirect:/admin/local-bans";
    }

    @PostMapping("/local-bans/add")
    public String adminLocalBansAdd(long bannedUser, long banThread, Model model) {
        if (usersRepository.searchById(bannedUser) != null && threadsRepository.searchById(banThread) != null) {
            LocalBanList localBan = new LocalBanList();
            localBan.setBannedUser(usersRepository.searchById(bannedUser));
            localBan.setBanThread(threadsRepository.searchById(banThread));
            localBanListRepository.save(localBan);
        }
        return "redirect:/admin/local-bans";
    }

    @GetMapping("/global-bans")
    public String adminGlobalBans(Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("globalbans", globalBanListRepository.findAll());
        return "AdminGlobalBans";
    }

    @PostMapping("/global-bans/search")
    public String adminGlobalBansSearch(String search, Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("globalbans", globalBanListRepository.searchByBannedUser(usersRepository.searchById(Long.valueOf(search))));
        return "AdminGlobalBans";
    }

    @GetMapping("/global-bans/delete/{id}")
    public String adminGlobalBansDelete(@PathVariable (value = "id") long id, Model model) {
        globalBanListRepository.delete(globalBanListRepository.searchById(id));
        return "redirect:/admin/global-bans";
    }

    @PostMapping("/global-bans/change/{id}")
    public String adminGlobalBansChange(@PathVariable (value = "id") long id, Date banStartDate, Date banExpireDate, long bannedUser, boolean loginBan, boolean writeBan, Model model) {
        if (banStartDate.before(banExpireDate) && usersRepository.searchById(bannedUser) != null) {
            GlobalBanList globanBan = globalBanListRepository.searchById(id);
            globanBan.setBannedUser(usersRepository.searchById(bannedUser));
            globanBan.setBanStartDate(banStartDate);
            globanBan.setBanExpireDate(banExpireDate);
            globanBan.setWriteBan(writeBan);
            globanBan.setLoginBan(loginBan);
            globalBanListRepository.save(globanBan);
        }
        return "redirect:/admin/global-bans";
    }

    @PostMapping("/global-bans/add")
    public String adminGlobalBansAdd(Date banStartDate, Date banExpireDate, long bannedUser, boolean loginBan, boolean writeBan, Model model) {
        if (banStartDate.before(banExpireDate) && usersRepository.searchById(bannedUser) != null) {
            GlobalBanList globanBan = new GlobalBanList();
            globanBan.setBannedUser(usersRepository.searchById(bannedUser));
            globanBan.setBanStartDate(banStartDate);
            globanBan.setBanExpireDate(banExpireDate);
            globanBan.setWriteBan(writeBan);
            globanBan.setLoginBan(loginBan);
            globalBanListRepository.save(globanBan);
        }
        return "redirect:/admin/global-bans";
    }

    @GetMapping("/notifications")
    public String adminNotificationsPageGet(Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("notifications", notificationsRepository.findAll());
        return "AdminNotifications";
    }

    @PostMapping("/notifications/searchByText")
    public String adminNotificationsSearchByText(String search, Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("notifications", notificationsRepository.searchByText(search));
        return "AdminNotifications";
    }

    @PostMapping("/notifications/searchByMessageId")
    public String adminNotificationsSearchByMessageId(Long search, Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("notifications", notificationsRepository.searchByMessage(messagesRepository.searchById(search)));
        return "AdminNotifications";
    }

    @PostMapping("/notifications/searchByUserId")
    public String adminNotificationsSearchByUserId(Long search, Model model) {
        model.addAttribute("title","Админ-панель");
        model.addAttribute("notifications", notificationsRepository.searchByUser(usersRepository.searchById(search)));
        return "AdminNotifications";
    }

    @GetMapping("/notifications/delete/{id}")
    public String adminNotificationsDelete(@PathVariable (value = "id") Long id, Model model) {
        notificationsRepository.delete(notificationsRepository.searchById(id));
        return "redirect:/admin/notifications";
    }

    @PostMapping("/notifications/change/{id}")
    public String adminNotificationsChange(@PathVariable (value = "id") Long id, String text, Boolean read, Long message, Long user, Model model) {
        if (text != "" && message != null & user != null) {
            Notifications notification = notificationsRepository.searchById(id);
            notification.setText(text);
            notification.setNotificationRead(read);
            notification.setMessage(messagesRepository.searchById(message));
            notification.setUser(usersRepository.searchById(user));
            notificationsRepository.save(notification);
        }
        model.addAttribute("title","Админ-панель");
        return "redirect:/admin/notifications";
    }

    @PostMapping("/notifications/add")
    public String adminNotificationsAdd(String text, Boolean read, Long message, Long user, Model model) {
        if (text != "" && message != null & user != null) {
            Notifications notification = new Notifications();
            notification.setText(text);
            notification.setNotificationRead(read);
            notification.setMessage(messagesRepository.searchById(message));
            notification.setUser(usersRepository.searchById(user));
            notificationsRepository.save(notification);
        }
        model.addAttribute("title","Админ-панель");
        return "redirect:/admin/notifications";
    }

}
