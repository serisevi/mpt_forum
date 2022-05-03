package com.example.forummpt.controllers;

import com.example.forummpt.dto.*;
import com.example.forummpt.models.*;
import com.example.forummpt.repo.*;
import com.example.forummpt.services.ThreadsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    //region repositories
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ThreadsRepo threadsRepository;
    @Autowired private GlobalBanListRepo globalBanListRepository;
    @Autowired private LocalBanListRepo localBanListRepository;
    @Autowired private MessagesRepo messagesRepository;
    @Autowired private MessageImageRepo messageImageRepository;
    @Autowired private NotificationsRepo notificationsRepository;
    @Autowired private PersonalInformationRepo personalInformationRepository;
    @Autowired private ResetCodesRepo resetCodesRepository;
    @Autowired private SpecializationsRepo specializationsRepository;
    @Autowired private UsersRepo usersRepository;
    private final ThreadsService threadsService;
    @Autowired public ApiController(ThreadsService threadsService) {
        this.threadsService = threadsService;
    }
    //endregion

    //region Threads
    @GetMapping("/threads")
    public List<ThreadDTO> findAllThreads() {
        List<Threads> list = threadsRepository.findAll();
        List<ThreadDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new ThreadDTO(list.get(i))); }
        return dto;
    }

    @GetMapping("/threads/count-pages")
    public String getThreadsCount() {
        Double threadCount = Double.valueOf(threadsRepository.count());
        Integer pagesCount = (int) Math.ceil(threadCount/10.0);
        return String.valueOf(pagesCount);
    }

    @GetMapping("/threads/page/{id}")
    public List<ThreadDTO> getThreadsByPage(@PathVariable(value = "id") Integer id) {
        List<Threads> list = threadsService.getPage(id, 10).getPage().getContent();
        List<ThreadDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new ThreadDTO(list.get(i))); }
        return dto;
    }

    @PostMapping("/threads/search")
    public List<ThreadDTO> searchThreads(String text) {
        List<Threads> list = threadsRepository.searchByThreadAuthor_UsernameOrThreadNameContainsOrThreadDescriptionContains(text, text, text);
        List<ThreadDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new ThreadDTO(list.get(i))); }
        return dto;
    }

    @GetMapping("/threads/{id}")
    public ThreadDTO findThreadById(@PathVariable(value = "id") Long id) { return new ThreadDTO(threadsRepository.searchById(id)); }

    @GetMapping("/threads/user/{id}")
    public List<ThreadDTO> findThreadsByUserId(@PathVariable(value = "id") Long id) {
        List<Threads> list = threadsRepository.searchByThreadAuthor_Id(id);
        List<ThreadDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new ThreadDTO(list.get(i))); }
        return dto;
    }

    @PostMapping("/threads/post")
    public String postThread(String name, String description, java.sql.Date creationTime, Long userId) {
        try {
            Threads thread = new Threads();
            thread.setThreadName(name);
            thread.setThreadDescription(description);
            thread.setThreadCreationTime(creationTime);
            thread.setThreadAuthor(usersRepository.searchById(userId));
            threadsRepository.save(thread);
            return "success";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("/threads/put")
    public String putThread(Long id, String name, String description, java.sql.Date creationTime, Long userId) {
        try {
            Threads thread = threadsRepository.searchById(id);
            if (thread != null) {
                thread.setThreadName(name);
                thread.setThreadDescription(description);
                thread.setThreadCreationTime(creationTime);
                thread.setThreadAuthor(usersRepository.searchById(userId));
                threadsRepository.save(thread);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/threads/delete")
    public String deleteThread(Long id) {
        try {
            Threads thread = threadsRepository.searchById(id);
            if (thread != null) {
                threadsRepository.delete(thread);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }
    //endregion

    //region Messages
    @GetMapping("/messages")
    public List<MessageDTO> findAllMessages() {
        List<Messages> list = messagesRepository.findAll();
        List<MessageDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new MessageDTO(list.get(i))); }
        return dto;
    }

    @GetMapping("/messages/app/thread/{id}")
    public List<MessageAppDTO> findMessagesByThreadForApp(@PathVariable(value = "id") Long id) {
        List<Messages> list = messagesRepository.searchByThread_Id(id);
        List<MessageAppDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new MessageAppDTO(list.get(i), messageImageRepository)); }
        return dto;
    }

    @GetMapping("/messages/{id}")
    public MessageDTO findMessageById(@PathVariable(value = "id") Long id) { return new MessageDTO(messagesRepository.searchById(id)); }

    @GetMapping("/messages/thread/{id}")
    public List<MessageDTO> findMessagesByThread(@PathVariable(value = "id") Long id) {
        List<Messages> list = messagesRepository.searchByThread_Id(id);
        List<MessageDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new MessageDTO(list.get(i))); }
        return dto;
    }

    @GetMapping("/messages/user/{id}")
    public List<MessageDTO> findMessagesByUser(@PathVariable(value = "id") Long id) {
        List<Messages> list = messagesRepository.searchByMessageAuthor_Id(id);
        List<MessageDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new MessageDTO(list.get(i))); }
        return dto;
    }

    @PostMapping("/messages/post")
    public String postMessage(String text, Timestamp datetime, Long threadId, Long userId, Long replyId) {
        try {
            Messages message = new Messages();
            message.setMessageText(text);
            message.setMessageDatetime(datetime);
            message.setThread(threadsRepository.searchById(threadId));
            message.setMessageAuthor(usersRepository.searchById(userId));
            message.setMessageReply(messagesRepository.searchById(replyId));
            messagesRepository.save(message);
            return "success";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("/messages/put")
    public String putMessage(Long id, String text, Timestamp datetime, Long threadId, Long userId, Long replyId) {
        try {
            Messages message = messagesRepository.searchById(id);
            if (message != null) {
                message.setMessageText(text);
                message.setMessageDatetime(datetime);
                message.setThread(threadsRepository.searchById(threadId));
                message.setMessageAuthor(usersRepository.searchById(userId));
                message.setMessageReply(messagesRepository.searchById(replyId));
                messagesRepository.save(message);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/messages/delete")
    public String deleteMessage(Long id) {
        try {
            Messages message = messagesRepository.searchById(id);
            if (message != null) {
                messagesRepository.delete(message);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }
    //endregion

    //region Users
    @GetMapping("/users")
    public List<UserDTO> findAllUsers() {
        List<User> list = usersRepository.findAll();
        List<UserDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new UserDTO(list.get(i))); }
        return dto;
    }

    @GetMapping("/users/{id}")
    public UserDTO findUserById(@PathVariable(value = "id") Long id) { return new UserDTO(usersRepository.searchById(id)); }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/users/post")
    public String postUser(String username, String password, String email, java.sql.Date datetime, Boolean active, Long userInfoId) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setDatetime(datetime);
            user.setActive(active);
            user.setUserInfo(personalInformationRepository.searchById(userInfoId));
            usersRepository.save(user);
            return "success";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("/users/put")
    public String putUser(Long id, String username, String password, String email, java.sql.Date datetime, Boolean active, Long userInfoId) {
        try {
            User user = usersRepository.searchById(id);
            if (user != null) {
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(password));
                user.setDatetime(datetime);
                user.setActive(active);
                user.setUserInfo(personalInformationRepository.searchById(userInfoId));
                usersRepository.save(user);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/users/delete")
    public String deleteUser(Long id) {
        try {
            User user = usersRepository.searchById(id);
            if (user != null) {
                usersRepository.delete(user);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }
    //endregion

    //region PersonalInformation
    @GetMapping("/personal-info")
    public List<UserInfoDTO> findAllPersonalInfo() {
        List<PersonalInformation> list = personalInformationRepository.findAll();
        List<UserInfoDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new UserInfoDTO(list.get(i))); }
        return dto;
    }

    @GetMapping("/personal-info/{id}")
    public UserInfoDTO findPersonalInfoById(@PathVariable(value = "id") Long id) { return new UserInfoDTO(personalInformationRepository.searchById(id)); }

    @GetMapping("/personal-info/user/{id}")
    public UserInfoDTO findPersonalInfoByUser(@PathVariable(value = "id") Long id) { return new UserInfoDTO(usersRepository.searchById(id).getUserInfo()); }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/personal-info/post")
    public String postPersonalInfo(String firstname, String middlename, String lastname, String description, String imageUrl, Integer course, Long specializationId) {
        try {
            PersonalInformation personalInformation = new PersonalInformation();
            personalInformation.setFirstname(firstname);
            personalInformation.setMiddlename(middlename);
            personalInformation.setLastname(lastname);
            personalInformation.setDescription(description);
            personalInformation.setImageUrl(imageUrl);
            personalInformation.setCourse(course);
            personalInformation.setSpecialization(specializationsRepository.searchById(specializationId));
            personalInformationRepository.save(personalInformation);
            return "success";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("/personal-info/put")
    public String putPersonalInfo(Long id, String firstname, String middlename, String lastname, String description, String imageUrl, Integer course, Long specializationId) {
        try {
            PersonalInformation personalInformation = personalInformationRepository.searchById(id);
            if (personalInformation != null) {
                personalInformation.setFirstname(firstname);
                personalInformation.setMiddlename(middlename);
                personalInformation.setLastname(lastname);
                personalInformation.setDescription(description);
                personalInformation.setImageUrl(imageUrl);
                personalInformation.setCourse(course);
                personalInformation.setSpecialization(specializationsRepository.searchById(specializationId));
                personalInformationRepository.save(personalInformation);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/personal-info/delete")
    public String deletePersonalInfo(Long id) {
        try {
            PersonalInformation personalInformation = personalInformationRepository.searchById(id);
            if (personalInformation != null) {
                personalInformationRepository.delete(personalInformation);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }
    //endregion

    //region Specializations
    @GetMapping("/specializations")
    public List<SpecializationDTO> findAllSpecializations() {
        List<Specializations> list = specializationsRepository.findAll();
        List<SpecializationDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new SpecializationDTO(list.get(i))); }
        return dto;
    }

    @GetMapping("/specializations/{id}")
    public SpecializationDTO findSpecizalizationById(@PathVariable(value = "id") Long id) { return new SpecializationDTO(specializationsRepository.searchById(id)); }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/specializations/post")
    public String postSpecialization(String specialization) {
        try {
            Specializations spec = new Specializations();
            spec.setSpecialization(specialization);
            specializationsRepository.save(spec);
            return "success";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("/specializations/put")
    public String putSpecialization(Long id, String specialization) {
        try {
            Specializations spec = specializationsRepository.searchById(id);
            if (spec != null) {
                spec.setSpecialization(specialization);
                specializationsRepository.save(spec);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/specializations/delete")
    public String deleteSpecialization(Long id) {
        try {
            Specializations specialization = specializationsRepository.searchById(id);
            if (specialization != null) {
                specializationsRepository.delete(specialization);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }
    //endregion

    //region Notifications
    @GetMapping("/notifications/me")
    public List<NotificationDTO> findMyNotifications() {
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName().toString());
        List<Notifications> list = notificationsRepository.searchByUser_Id(user.getId());
        List<NotificationDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new NotificationDTO(list.get(i))); }
        return dto;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/notifications")
    public List<NotificationDTO> findAllNotifications() {
        List<Notifications> list = notificationsRepository.findAll();
        List<NotificationDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new NotificationDTO(list.get(i))); }
        return dto;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/notifications/{id}")
    public NotificationDTO findNotificationById(@PathVariable(value = "id") Long id) { return new NotificationDTO(notificationsRepository.searchById(id)); }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/notifications/user/{id}")
    public List<NotificationDTO> findNotificationByUserId(@PathVariable(value = "id") Long id) {
        List<Notifications> list = notificationsRepository.searchByUser_Id(id);
        List<NotificationDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new NotificationDTO(list.get(i))); }
        return dto;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/notifications/post")
    public String postNotification(String text, Boolean read, Long userId, Long messageId) {
        try {
            Notifications notification = new Notifications();
            notification.setText(text);
            notification.setNotificationRead(read);
            notification.setUser(usersRepository.searchById(userId));
            notification.setMessage(messagesRepository.searchById(messageId));
            notificationsRepository.save(notification);
            return "success";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("/notifications/put")
    public String putNotification(Long id, String text, Boolean read, Long userId, Long messageId) {
        try {
            Notifications notification = notificationsRepository.searchById(id);
            if (notification != null) {
                notification.setText(text);
                notification.setNotificationRead(read);
                notification.setUser(usersRepository.searchById(userId));
                notification.setMessage(messagesRepository.searchById(messageId));
                notificationsRepository.save(notification);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/notifications/delete")
    public String deleteNotification(Long id) {
        try {
            Notifications notification = notificationsRepository.searchById(id);
            if (notification != null) {
                notificationsRepository.delete(notification);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }
    //endregion

    //region LocalBans
    @GetMapping("/local-bans/me")
    public List<LocalBanDTO> findMyLocalBans() {
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName().toString());
        List<LocalBanList> list = localBanListRepository.searchByBannedUser_Id(user.getId());
        List<LocalBanDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new LocalBanDTO(list.get(i))); }
        return dto;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("local-bans/all")
    public List<LocalBanDTO> findAllLocalBans() {
        List<LocalBanList> list = localBanListRepository.findAll();
        List<LocalBanDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new LocalBanDTO(list.get(i))); }
        return dto;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("local-bans/{id}")
    public LocalBanDTO findLocalBanById(@PathVariable(value = "id") Long id) {
        return new LocalBanDTO(localBanListRepository.searchById(id));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("local-bans/thread/{id}")
    public List<LocalBanDTO> findLocalBansByThreadId(@PathVariable(value = "id") Long id) {
        List<LocalBanList> list = localBanListRepository.searchByBanThread_Id(id);
        List<LocalBanDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new LocalBanDTO(list.get(i))); }
        return dto;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("local-bans/user/{id}")
    public List<LocalBanDTO> findLocalBansByUserId(@PathVariable(value = "id") Long id) {
        List<LocalBanList> list = localBanListRepository.searchByBannedUser_Id(id);
        List<LocalBanDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new LocalBanDTO(list.get(i))); }
        return dto;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("local-bans/post")
    public String postLocalBan(Long userId, Long threadId) {
        try {
            LocalBanList localBan = new LocalBanList();
            localBan.setBannedUser(usersRepository.searchById(userId));
            localBan.setBanThread(threadsRepository.searchById(threadId));
            localBanListRepository.save(localBan);
            return "success";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("local-bans/put")
    public String putLocalBan(Long id, Long userId, Long threadId) {
        try {
            LocalBanList localBan = localBanListRepository.searchById(id);
            if (localBan != null) {
                localBan.setBannedUser(usersRepository.searchById(userId));
                localBan.setBanThread(threadsRepository.searchById(threadId));
                localBanListRepository.save(localBan);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("local-bans/delete")
    public String deleteLocalBan(Long id) {
        try {
            LocalBanList localBan = localBanListRepository.searchById(id);
            if (localBan != null) {
                localBanListRepository.delete(localBan);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }
    //endregion

    //region GlobalBans
    @GetMapping("/global-bans/me")
    public GlobalBanDTO findMyGlobalBan() {
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName().toString());
        return new GlobalBanDTO(globalBanListRepository.searchByBannedUser_Id(user.getId()));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/global-bans")
    public List<GlobalBanDTO> findAllGlobalBans() {
        List<GlobalBanList> list = globalBanListRepository.findAll();
        List<GlobalBanDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new GlobalBanDTO(list.get(i))); }
        return dto;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/global-bans/{id}")
    public GlobalBanDTO findGlobalBanById(@PathVariable(value = "id") Long id) { return new GlobalBanDTO(globalBanListRepository.searchById(id)); }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/global-bans/user/{id}")
    public GlobalBanDTO findGlobalBanByUserId(@PathVariable(value = "id") Long id) { return new GlobalBanDTO(globalBanListRepository.searchByBannedUser_Id(id)); }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/global-bans/post")
    public String postGlobalBan(Boolean loginBan, Boolean writeBan, java.sql.Date banStartDate, java.sql.Date banExpireDate, Long userId) {
        try {
            GlobalBanList globalBan = new GlobalBanList();
            globalBan.setLoginBan(loginBan);
            globalBan.setWriteBan(writeBan);
            globalBan.setBanStartDate(banStartDate);
            globalBan.setBanExpireDate(banExpireDate);
            globalBan.setBannedUser(usersRepository.searchById(userId));
            globalBanListRepository.save(globalBan);
            return "success";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("/global-bans/put")
    public String putGlobalBan(Long id, Boolean loginBan, Boolean writeBan, java.sql.Date banStartDate, java.sql.Date banExpireDate, Long userId) {
        try {
            GlobalBanList globalBan = globalBanListRepository.searchById(id);
            if (globalBan != null) {
                globalBan.setLoginBan(loginBan);
                globalBan.setWriteBan(writeBan);
                globalBan.setBanStartDate(banStartDate);
                globalBan.setBanExpireDate(banExpireDate);
                globalBan.setBannedUser(usersRepository.searchById(userId));
                globalBanListRepository.save(globalBan);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/global-ban/delete")
    public String deleteGlobalBan(Long id) {
        try {
            GlobalBanList globalBan = globalBanListRepository.searchById(id);
            if (globalBan != null) {
                globalBanListRepository.delete(globalBan);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }
    //endregion

    //region MessageImages
    @GetMapping("/images")
    public List<MessageImagesDTO> findAllImages() {
        List<MessageImages> list = messageImageRepository.findAll();
        List<MessageImagesDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new MessageImagesDTO(list.get(i))); }
        return dto;
    }

    @GetMapping("/images/{id}")
    public MessageImagesDTO findImageById(@PathVariable(value = "id") Long id) { return new MessageImagesDTO(messageImageRepository.searchById(id)); }

    @GetMapping("/images/message/{id}")
    public List<MessageImagesDTO> findImageByMessageId(@PathVariable(value = "id") Long id) {
        List<MessageImages> list = messageImageRepository.searchByMessage_IdOrderById(id);
        List<MessageImagesDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new MessageImagesDTO(list.get(i))); }
        return dto;
    }

    @PostMapping("/images/post")
    public String postMessageImage(Long messageId, String imageUrl) {
        try {
            MessageImages messageImage = new MessageImages();
            messageImage.setMessage(messagesRepository.searchById(messageId));
            messageImage.setImageUrl(imageUrl);
            messageImageRepository.save(messageImage);
            return "success";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("/images/put")
    public String putMessageImage(Long id, Long messageId, String imageUrl) {
        try {
            MessageImages messageImage = messageImageRepository.searchById(id);
            if (messageImage != null) {
                messageImage.setMessage(messagesRepository.searchById(messageId));
                messageImage.setImageUrl(imageUrl);
                messageImageRepository.save(messageImage);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/images/delete")
    public String deleteMessageImage(Long id) {
        try {
            MessageImages messageImage = messageImageRepository.searchById(id);
            if (messageImage != null) {
                messageImageRepository.delete(messageImage);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }
    //endregion

    //region ResetCodes
    @GetMapping("/reset-codes/me")
    public ResetCodeDTO findMyResetCode() {
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName().toString());
        return new ResetCodeDTO(resetCodesRepository.searchByUser_Id(user.getId()));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/reset-codes/all")
    public List<ResetCodeDTO> findAllResetCodes() {
        List<ResetCodes> list = resetCodesRepository.findAll();
        List<ResetCodeDTO> dto = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) { dto.add(new ResetCodeDTO(list.get(i))); }
        return dto;
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/reset-codes/{id}")
    public ResetCodeDTO findResetCodeById(@PathVariable(value = "id") Long id) { return new ResetCodeDTO(resetCodesRepository.searchById(id)); }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @GetMapping("/reset-codes/user/{id}")
    public ResetCodeDTO findResetCodeByUserId(@PathVariable(value = "id") Long id) { return new ResetCodeDTO(resetCodesRepository.searchByUser_Id(id)); }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping("/reset-codes/post")
    public String postResetCode(String resetCode, Long userId) {
        try {
            ResetCodes resetCodes = new ResetCodes();
            resetCodes.setResetCode(resetCode);
            resetCodes.setUser(usersRepository.searchById(userId));
            resetCodesRepository.save(resetCodes);
            return "success";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PutMapping("/reset-codes/put")
    public String putResetCode(Long id, String resetCode, Long userId) {
        try {
            ResetCodes resetCodes = resetCodesRepository.searchById(id);
            if (resetCodes != null) {
                resetCodes.setResetCode(resetCode);
                resetCodes.setUser(usersRepository.searchById(userId));
                resetCodesRepository.save(resetCodes);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @DeleteMapping("/reset-codes/delete")
    public String deleteResetCode(Long id) {
        try {
            ResetCodes resetCodes = resetCodesRepository.searchById(id);
            if (resetCodes != null) {
                resetCodesRepository.delete(resetCodes);
                return "success";
            }
            return "error";
        }
        catch (Exception e) { return "error"; }
    }
    //endregion
}
