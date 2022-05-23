package com.example.forummpt.controllers;

import com.example.forummpt.dto.*;
import com.example.forummpt.models.*;
import com.example.forummpt.repo.*;
import com.example.forummpt.services.*;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
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
    @Autowired private ApiSessionsRepo apiSessionsRepository;
    private final ThreadsService threadsService;
    private final MessagesService messagesService;
    @Autowired public ApiController(MessagesService messagesService, ThreadsService threadsService) {
        this.messagesService = messagesService;
        this.threadsService = threadsService;
    }
    //endregion

    //region App

    public Boolean validateToken(String token) {
        ApiSessions session = apiSessionsRepository.searchByToken(token);
        if (session != null && session.getUser().isActive() == true) {
            GlobalBanList globalBan = globalBanListRepository.searchByBannedUser(session.getUser());
            if (globalBan != null) {
                if (globalBan.getBanExpireDate().after(new java.util.Date())) { return false; }
                else { return true; }
            }
            return true;
        }
        else { return  false; }
    }

    private void sendEmail(String recipient, String subject, String text) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.yandex.ru");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.port", 465);
        props.put("mail.smtp.auth", "true");
        Session session = Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("mptforumbot@yandex.ru","mptforum");
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("mptforumbot@yandex.ru"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setText(text);
            Transport.send(message);
        }
        catch (MessagingException e) { System.out.println(e.toString()); throw new RuntimeException(e); }
    }

    @PostMapping("/threads/count-pages")
    public ApiResponse getThreadsCount(String token) {
        if (validateToken(token) == true) {
            Double threadCount = Double.valueOf(threadsRepository.count());
            Integer pagesCount = (int) Math.ceil(threadCount/10.0);
            return new ApiResponse(pagesCount.toString());
        }
        return new ApiResponse("error");
    }

    @PostMapping("/threads/{id}")
    public ThreadDTO getThreadById(@PathVariable(value = "id") Long id, String token) {
        if (validateToken(token) == true) {
            Threads thread = threadsRepository.searchById(id);
            if (thread != null) { return new ThreadDTO(thread); }
        }
        return new ThreadDTO();
    }

    @PostMapping("/threads/page/{id}")
    public List<ThreadDTO> getThreadsByPage(@PathVariable(value = "id") Integer id, String token) {
        List<ThreadDTO> dto = new ArrayList<>();
        if (validateToken(token) == true) {
            List<Threads> list = threadsService.getPage(id, 10).getPage().getContent();
            for (int i = 0; i < list.size(); i++) { dto.add(new ThreadDTO(list.get(i))); }
            return dto;
        }
        return dto;
    }

    @PostMapping("/threads/search")
    public List<ThreadDTO> searchThreads(String token, String text) {
        List<ThreadDTO> dto = new ArrayList<>();
        if (validateToken(token) == true) {
            List<Threads> list = threadsRepository.searchByThreadAuthor_UsernameOrThreadNameContainsOrThreadDescriptionContains(text, text, text);
            for (int i = 0; i < list.size(); i++) { dto.add(new ThreadDTO(list.get(i))); }
            return dto;
        }
        return dto;
    }

    @PostMapping("/threads/post")
    public ApiResponse postThread(String token, String name, String description, java.sql.Date creationTime, Long userId) {
        if (validateToken(token) == true) {
            try {
                Threads thread = new Threads();
                thread.setThreadName(name);
                thread.setThreadDescription(description);
                thread.setThreadCreationTime(creationTime);
                thread.setThreadAuthor(usersRepository.searchById(userId));
                threadsRepository.save(thread);
                return new ApiResponse("success");
            }
            catch (Exception e) { return new ApiResponse("error"); }
        }
        return new ApiResponse("error");
    }

    @RequestMapping(path = "/messages/images/upload", method = RequestMethod.POST, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE } )
    public ApiResponse uploadImage(@RequestParam MultipartFile file, @RequestParam Long messageId, @RequestParam String token) {
        if (validateToken(token) == true) {
            try {
                String uploadDir = "uploaded-images/";
                int idName = (int) messageImageRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).get(0).getId();
                idName++;
                FileUploadService.saveFile(uploadDir, String.valueOf(idName)+".jpg", file);
                String imagePath = "/"+uploadDir+idName+".jpg";
                MessageImages messageImage = new MessageImages();
                messageImage.setMessage(messagesRepository.searchById(messageId));
                messageImage.setImageUrl(imagePath);
                messageImageRepository.save(messageImage);
                return new ApiResponse("success");
            }
            catch (IOException e) { throw new RuntimeException(e); }
        }
       return new ApiResponse("error");
    }

    @RequestMapping(path = "/users/images/upload", method = RequestMethod.POST, consumes = { MediaType.MULTIPART_FORM_DATA_VALUE } )
    public ApiResponse uploadAvatar(@RequestParam MultipartFile file, @RequestParam Long userId) {
        try {
            String uploadDir = "uploaded-images/";
            String idName = "user"+userId.toString();
            FileUploadService.saveFile(uploadDir, idName+".jpg", file);
            return new ApiResponse("success");
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    @PostMapping("/stats/user/{id}")
    public StatsDTO getStats(@PathVariable(value = "id") Long id, String token) {
        User user = usersRepository.searchById(id);
        StatsDTO stats = new StatsDTO();
        if (validateToken(token) == true) {
            stats.setTotalMessagesCount(messagesRepository.searchByMessageAuthor(user).size());
            stats.setTotalThreadsCreated(threadsRepository.searchByThreadAuthor(user).size());
            Calendar cal = Calendar.getInstance();
            Timestamp currentDate = new Timestamp(cal.getTimeInMillis());
            cal.add(Calendar.MONTH, -1);
            Timestamp previousDate = new Timestamp(cal.getTimeInMillis());
            stats.setMonthlyMessagesCount(messagesRepository.searchByMessageDatetimeBetweenAndMessageAuthor(previousDate, currentDate, user).size());
            stats.setMonthlyThreadsCreated(threadsRepository.searchByThreadCreationTimeBetweenAndThreadAuthor(previousDate, currentDate, user).size());
            return stats;
        }
        return stats;
    }

    @PostMapping("/change-password/app")
    public ApiResponse changePasswordForApp(String token, String oldPassword, String newPassword, Long userId) throws JSONException {
        if (oldPassword != null && oldPassword.length() >= 8 && newPassword != null && newPassword.length() >= 8 && validateToken(token) == true) {
            User user = usersRepository.searchById(userId);
            boolean checker = passwordEncoder.matches(oldPassword, user.getPassword());
            if (checker == true) {
                user.setPassword(passwordEncoder.encode(newPassword));
                usersRepository.save(user);
                return new ApiResponse("success");
            }
            else { return new ApiResponse("error"); }
        }
        return new ApiResponse("error");
    }

    @PostMapping("/messages/thread/{id}/count-pages")
    public ApiResponse getThreadMessagePagesCount(@PathVariable (value = "id") Long id, String token) {
        if (validateToken(token) == true) {
            Double threadCount = Double.valueOf(messagesRepository.searchByThread_Id(id).size());
            Integer pagesCount = (int) Math.ceil(threadCount/10.0);
            return new ApiResponse(pagesCount.toString());
        }
        return new ApiResponse("error");
    }

    @PostMapping("/messages/app/thread/{id}/page/{id2}")
    public List<MessageAppDTO> findMessagesByThreadForApp(@PathVariable(value = "id") Long threadId, @PathVariable(value = "id2") Long page, String token) {
        List<MessageAppDTO> dto = new ArrayList<>();
        if (validateToken(token) == true) {
            List<Messages> list = messagesService.getPage(Integer.valueOf(page.toString()), 10, threadsRepository.searchById(threadId)).getPage().getContent();
            for (int i = 0; i < list.size(); i++) { dto.add(new MessageAppDTO(list.get(i), messageImageRepository)); }
            return dto;
        }
        return dto;
    }

    @PostMapping("/messages/app/thread/{id}/search")
    public List<MessageAppDTO> searchMessagesByThreadForApp(@PathVariable(value = "id") Long id, String token, String text) {
        List<MessageAppDTO> dto = new ArrayList<>();
        if (validateToken(token) == true) {
            List<Messages> list = messagesRepository.searchByThread_IdAndMessageTextContainsOrMessageAuthor_Username(id, text, text);
            for (int i = 0; i < list.size(); i++) { dto.add(new MessageAppDTO(list.get(i), messageImageRepository)); }
            return dto;
        }
        return dto;
    }

    @PostMapping("/messages/post")
    public MessageDTO postMessage(String token, String text, Timestamp datetime, Long threadId, Long userId, Long replyId) throws Exception {
        if (validateToken(token) == true) {
            try {
                Messages message = new Messages();
                message.setMessageText(text);
                message.setMessageDatetime(datetime);
                message.setThread(threadsRepository.searchById(threadId));
                message.setMessageAuthor(usersRepository.searchById(userId));
                if (replyId != null) { message.setMessageReply(messagesRepository.searchById(replyId)); }
                messagesRepository.save(message);
                return new MessageDTO(message);
            }
            catch (Exception e) { throw new Exception(e); }
        }
        return new MessageDTO();
    }

    @PostMapping("/users/app/{id}")
    public UserAppDTO getUserForApp(@PathVariable(value = "id") Long id, String token) {
        if (validateToken(token) == true) {
            return new UserAppDTO(usersRepository.searchById(id));
        }
        return new UserAppDTO();
    }

    @PutMapping("/users/app/put")
    public ApiResponse putUserForApp(String token, Long userId, String firstname, String middlename, String lastname, String description, int course, String specialization) {
        User user = usersRepository.searchById(userId);
        if (validateToken(token) == true && user != null) {
            PersonalInformation userInfo = user.getUserInfo();
            userInfo.setFirstname(firstname);
            userInfo.setMiddlename(middlename);
            userInfo.setLastname(lastname);
            userInfo.setCourse(course);
            userInfo.setDescription(description);
            userInfo.setSpecialization(specializationsRepository.searchBySpecialization(specialization));
            personalInformationRepository.save(userInfo);
            return new ApiResponse("success");
        }
        return new ApiResponse("error");
    }

    @GetMapping("/specializations")
    public List<SpecializationDTO> findAllSpecializations() {
        List<SpecializationDTO> dto = new ArrayList<>();
        List<Specializations> list = specializationsRepository.findAll();
        for (int i = 0; i < list.size(); i++) { dto.add(new SpecializationDTO(list.get(i))); }
        return dto;
    }

    @PostMapping("/login")
    public ApiSessionsDTO loginAPI(String username, String password) {
        User user = usersRepository.searchByUsername(username);
        if (user != null) {
            if (passwordEncoder.matches(password, user.getPassword()) == true) {
                PasswordGenerator passwordGenerator = new PasswordGenerator.PasswordGeneratorBuilder().useDigits(true).useLower(true).useUpper(true).build();
                ApiSessions apiSessions = apiSessionsRepository.searchByUser_Id(user.getId());
                if (apiSessions == null) {
                    apiSessions = new ApiSessions();
                    apiSessions.setUser(user);
                    apiSessions.setToken(passwordGenerator.generate(50));
                    apiSessionsRepository.save(apiSessions);
                    return new ApiSessionsDTO(apiSessions, usersRepository);
                }
                return new ApiSessionsDTO(apiSessions, usersRepository);
            }
            return new ApiSessionsDTO();
        }
        return new ApiSessionsDTO();
    }

    @PostMapping("/logout")
    public ApiResponse logoutApi(String token) {
        if (validateToken(token) == true) {
            apiSessionsRepository.delete(apiSessionsRepository.searchByToken(token));
            return new ApiResponse("success");
        }
        else { return new ApiResponse("error"); }
    }

    @PostMapping("/registration")
    public ApiResponse registerApi(String username, String email, String password, String firstname, String middlename,
                                   String lastname, String description, Integer course, String specialization) {
        if (usersRepository.searchByEmail(email) == null && usersRepository.searchByUsername(username) == null) {
            java.util.Date utilDate = new java.util.Date();
            // Создание персональной информации
            PersonalInformation userInfo = new PersonalInformation();
            userInfo.setSpecialization(specializationsRepository.searchBySpecialization(specialization));
            userInfo.setCourse(course);
            userInfo.setLastname(lastname);
            userInfo.setMiddlename(middlename);
            userInfo.setFirstname(firstname);
            userInfo.setDescription(description);
            userInfo.setImageUrl("");
            personalInformationRepository.save(userInfo);
            // Создание пользователя
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRoles(Collections.singleton(Role.USER));
            user.setActive(false);
            user.setDatetime(new java.sql.Date(utilDate.getTime()));
            user.setUserInfo(userInfo);
            usersRepository.save(user);
            userInfo.setImageUrl("/uploaded-images/user"+user.getId()+".jpg");
            personalInformationRepository.save(userInfo);
            // Создание кода доступа
            String code = new PasswordGenerator.PasswordGeneratorBuilder().useDigits(true).useLower(true).useUpper(true).build().generate(8);
            ResetCodes newCode = new ResetCodes();
            newCode.setUser(user);
            newCode.setResetCode(code);
            resetCodesRepository.save(newCode);
            String subject = "Подтверждение аккаунта";
            String text = "Вы зарегестрировались на форуме мпт. Для подтверждения аккаунта введите код: "+code;
            sendEmail(email, subject, text);
            // Ответ
            return new ApiResponse(String.valueOf(user.getId()));
        }
        return new ApiResponse("error");
    }

    @PostMapping("/registration/confirm")
    public ApiResponse registrationConfirmApi(String code) {
        ResetCodes resetCode = resetCodesRepository.searchByResetCode(code);
        if (resetCode != null) {
            User user = resetCode.getUser();
            user.setActive(true);
            usersRepository.save(user);
            resetCodesRepository.delete(resetCode);
            return new ApiResponse("success");
        }
        return new ApiResponse("error");
    }

    @PostMapping("/restoration")
    public ApiResponse restorationApi(String email) {
        User user = usersRepository.searchByEmail(email);
        if (user != null) {
            if (user.isActive() == true && resetCodesRepository.searchByUser(user) == null) {
                String code = new PasswordGenerator.PasswordGeneratorBuilder()
                        .useDigits(true).useLower(true).useUpper(true).build().generate(8);
                ResetCodes newCode = new ResetCodes();
                newCode.setUser(user);
                newCode.setResetCode(code);
                resetCodesRepository.save(newCode);
                String subject = "Восстановление пароля";
                String text = "Ваш код для восстановления: "+code;
                sendEmail(email, subject, text);
                return new ApiResponse("success");
            }
            return new ApiResponse("error");
        }
        return new ApiResponse("error");
    }

    @PostMapping("/restoration/confirm")
    public ApiResponse restorationConfirmApi(String code, String password) {
        ResetCodes resetCode = resetCodesRepository.searchByResetCode(code);
        if (resetCode != null) {
            User user = resetCode.getUser();
            user.setPassword(passwordEncoder.encode(password));
            usersRepository.save(user);
            resetCodesRepository.delete(resetCode);
            return new ApiResponse("success");
        }
        return new ApiResponse("error");
    }

    @PostMapping("/message/delete")
    public ApiResponse deleteMessage(String token, Long messageId) throws IOException {
        if (validateToken(token) == true) {
            User user = apiSessionsRepository.searchByToken(token).getUser();
            Messages message = messagesRepository.searchById(messageId);
            if (user.getRoles().toString().contains("ADMIN")) {
                message.setMessageText("Сообщение было удалено");
                messagesRepository.save(message);
                List<MessageImages> images = messageImageRepository.searchByMessage_IdOrderById(messageId);
                String uploadDir = "uploaded-images/";
                for (int i = 0; i < images.size(); i++) {
                    MessageImages it = images.get(i);
                    FileUploadService.deleteFile(uploadDir, it.getImageUrl().replace("/uploaded-images/", ""));
                    messageImageRepository.delete(it);
                }
                return new ApiResponse("success");
            }
        }
        return new ApiResponse("error");
    }

    @PostMapping("/local-ban/apply")
    public ApiResponse localBanUser(String token, Long messageId) {
        if (validateToken(token) == true) {
            Messages message =  messagesRepository.searchById(messageId);
            Long bannedUserId = message.getMessageAuthor().getId();
            User user = apiSessionsRepository.searchByToken(token).getUser();
            User bannedUser = usersRepository.searchById(bannedUserId);
            Threads thread = message.getThread();
            if (user.getId() == bannedUserId || bannedUser == thread.getThreadAuthor() || bannedUser.getRoles().toString().contains("ADMIN")) { return new ApiResponse("error"); }
            if (user.getRoles().toString().contains("ADMIN") || thread.getThreadAuthor() == user) {
                LocalBanList localBan = localBanListRepository.searchByBanThreadAndBannedUser(thread, bannedUser);
                if (localBan == null) {
                    localBan = new LocalBanList();
                    localBan.setBanThread(thread);
                    localBan.setBannedUser(bannedUser);
                    localBanListRepository.save(localBan);
                }
                return new ApiResponse("success");
            }
        }
        return new ApiResponse("error");
    }

    @PostMapping("/local-ban/unban")
    public ApiResponse localUnbanUser(String token, Long messageId) {
        if (validateToken(token) == true) {
            Messages message =  messagesRepository.searchById(messageId);
            Long bannedUserId = message.getMessageAuthor().getId();
            Threads thread = threadsRepository.searchById(message.getThread().getId());
            User user = apiSessionsRepository.searchByToken(token).getUser();
            User bannedUser = usersRepository.searchById(bannedUserId);
            LocalBanList localBan = localBanListRepository.searchByBanThreadAndBannedUser(thread, bannedUser);
            if (localBan != null && user.getRoles().toString().contains("ADMIN")) {
                localBanListRepository.delete(localBan);
                return new ApiResponse("success");
            }
        }
        return new ApiResponse("error");
    }

    @PostMapping("/global-ban/apply")
    public ApiResponse globalBanUser(String token, Long messageId, java.sql.Date start, java.sql.Date end, boolean loginBan, boolean writeBan) {
        if (validateToken(token) == true) {
            Messages message = messagesRepository.searchById(messageId);
            User user = apiSessionsRepository.searchByToken(token).getUser();
            if (end.before(start) || user.getId() == message.getMessageAuthor().getId()) { return new ApiResponse("error"); }
            User bannedUser = usersRepository.searchById(message.getMessageAuthor().getId());
            if (user.getRoles().toString().contains("ADMIN")) {
                GlobalBanList globalBan = globalBanListRepository.searchByBannedUser(bannedUser);
                if (globalBan == null) { globalBan = new GlobalBanList(); }
                globalBan.setBannedUser(bannedUser);
                globalBan.setBanStartDate(start);
                globalBan.setBanExpireDate(end);
                globalBan.setLoginBan(loginBan);
                globalBan.setWriteBan(writeBan);
                globalBanListRepository.save(globalBan);
                return new ApiResponse("success");
            }
        }
        return new ApiResponse("error");
    }

    @PostMapping("/global-ban/unban")
    public ApiResponse globalUnbanUser(String token, Long messageId) {
        if (validateToken(token) == true) {
            User user = apiSessionsRepository.searchByToken(token).getUser();
            User bannedUser = messagesRepository.searchById(messageId).getMessageAuthor();
            if (user.getRoles().toString().contains("ADMIN")) {
                GlobalBanList globalBan = globalBanListRepository.searchByBannedUser(bannedUser);
                if (globalBan != null) {
                    globalBanListRepository.delete(globalBan);
                    return new ApiResponse("success");
                }
            }
        }
        return new ApiResponse("error");
    }

    @PostMapping("/notifications/read")
    public List<NotificationDTO> getReadNotifications(String token) {
        List<NotificationDTO> dto = new ArrayList<>();
        if (validateToken(token) == true) {
            ApiSessions session = apiSessionsRepository.searchByToken(token);
            List<Notifications> list = notificationsRepository.searchByUserAndNotificationRead(session.getUser(), true);
            for (int i = 0; i < list.size(); i++) { dto.add(new NotificationDTO(list.get(i))); }
            return dto;
        }
        return dto;
    }

    @PostMapping("/notifications/unread")
    public List<NotificationDTO> getUnreadNotifications(String token) {
        List<NotificationDTO> dto = new ArrayList<>();
        if (validateToken(token) == true) {
            ApiSessions session = apiSessionsRepository.searchByToken(token);
            List<Notifications> list = notificationsRepository.searchByUserAndNotificationRead(session.getUser(), false);
            for (int i = 0; i < list.size(); i++) { dto.add(new NotificationDTO(list.get(i))); }
            return dto;
        }
        return dto;
    }

    @PostMapping("/notifications/go-to")
    public ApiResponse getNotificationThreadAndPage(String token, Long notificationId) {
        if (validateToken(token) == true) {
            Notifications notification = notificationsRepository.searchById(notificationId);
            if (notification != null) {
                notification.setNotificationRead(true);
                notificationsRepository.save(notification);
                Long threadId = notification.getMessage().getThread().getId();
                int number = (int) Math.ceil((messagesRepository.searchByThread(notification.getMessage().getThread()).indexOf(notification.getMessage())+1.0)/10.0);
                return new ApiResponse(String.valueOf(threadId)+","+String.valueOf(number));
            }
        }
        return new ApiResponse("error");
    }

    @PostMapping("/notifications/read/clear")
    public ApiResponse clearReadNotifications(String token) {
        if (validateToken(token) == true) {
            ApiSessions session = apiSessionsRepository.searchByToken(token);
            if (session != null) {
                List<Notifications> notifications = notificationsRepository.searchByUserAndNotificationRead(session.getUser(), true);
                for (int i = 0; i < notifications.size(); i++) {
                    notificationsRepository.delete(notifications.get(i));
                }
                return new ApiResponse("success");
            }
        }
        return new ApiResponse("error");
    }

    @PostMapping("/notifications/unread/clear")
    public ApiResponse clearUnreadNotifications(String token) {
        if (validateToken(token) == true) {
            ApiSessions session = apiSessionsRepository.searchByToken(token);
            if (session != null) {
                List<Notifications> notifications = notificationsRepository.searchByUserAndNotificationRead(session.getUser(), false);
                for (int i = 0; i < notifications.size(); i++) {
                    notificationsRepository.delete(notifications.get(i));
                }
                return new ApiResponse("success");
            }
        }
        return new ApiResponse("error");
    }

    //endregion

    /*

    //region Threads
    @GetMapping("/threads")
    public List<ThreadDTO> findAllThreads() {
        List<Threads> list = threadsRepository.findAll();
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

    */

}
