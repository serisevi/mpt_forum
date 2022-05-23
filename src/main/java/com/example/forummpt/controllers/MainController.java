package com.example.forummpt.controllers;

import com.example.forummpt.models.*;
import com.example.forummpt.repo.*;
import com.example.forummpt.services.FileUploadService;
import com.example.forummpt.services.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.*;
import java.util.List;

@Controller
public class MainController {

    //region Repositories
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UsersRepo usersRepository;
    @Autowired private MessagesRepo messagesRepository;
    @Autowired private PersonalInformationRepo personalInformationRepository;
    @Autowired private SpecializationsRepo specializationsRepository;
    @Autowired private GlobalBanListRepo globalBanListRepository;
    @Autowired private NotificationsRepo notificationsRepository;
    @Autowired private ResetCodesRepo resetCodesRepository;
    //endregion

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

    @GetMapping("/error")
    public String errorPageGet(Model model) {
        return "Error";
    }

    @PostMapping("/error")
    public String errorPagePost(Model model) {
        return "Error";
    }

    @GetMapping("/")
    public String authorizationPageGet(Model model){
        model.addAttribute("title","Авторизация");
        return "Authorization";
    }

    @PostMapping("/")
    public String authorizationPagePost(Model model){
        model.addAttribute("title","Авторизация");
        return "Authorization";
    }

    @GetMapping("/registration")
    public String registrationPageGet(Model model){
        model.addAttribute("title","Регистрация");
        List<Specializations> specializations = specializationsRepository.findAll();
        model.addAttribute("specializations", specializations);
        return "Registration";
    }

    @PostMapping("/registration")
    public String registrationPagePost(String username, String email, String password, String lastname, String firstname,
                                       String middlename, String description, Integer course, String specialization, Model model) {
        model.addAttribute("title","Регистрация");
        User userFromDb = usersRepository.searchByUsernameOrEmail(username, email);
        if (username.length() >= 6 && password.length() >= 8 && email.contains("@mpt.ru")) {
            if (userFromDb != null) {
                model.addAttribute("error","Данные уже используются");
                return "Registration";
            }
            else {
                PasswordGenerator passwordGenerator = new PasswordGenerator.PasswordGeneratorBuilder().useDigits(true).useLower(true).useUpper(true).build();
                String code = passwordGenerator.generate(8);
                String subject = "Подтверждение аккаунта";
                String text = "Вы зарегестрировались на форуме мпт. Для подтверждения аккаунта введите код: "+code;
                sendEmail(email, subject, text);
                java.util.Date utilDate = new java.util.Date();
                User user = new User();
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(password));
                user.setDatetime(new java.sql.Date(utilDate.getTime()));
                user.setActive(false);
                user.setRoles(Collections.singleton(Role.USER));
                PersonalInformation userInfo = new PersonalInformation();
                userInfo.setFirstname(firstname);
                userInfo.setMiddlename(middlename);
                userInfo.setLastname(lastname);
                userInfo.setDescription(description);
                Specializations spec = specializationsRepository.searchBySpecialization(specialization);
                userInfo.setSpecialization(spec);
                userInfo.setCourse(course);
                userInfo.setImageUrl("/uploaded-images/basicavatar.jpg");
                personalInformationRepository.save(userInfo);
                user.setUserInfo(userInfo);
                usersRepository.save(user);
                ResetCodes newCode = new ResetCodes();
                newCode.setUser(user);
                newCode.setResetCode(code);
                resetCodesRepository.save(newCode);
                return "redirect:/registration/confirm";
            }
        }
        else {
            model.addAttribute("error","Длина логина должна превышать 6 символов, пароля 8 символов, почтовый адрес должен содержать домен @mpt.ru");
            return "Registration";
        }
    }

    @GetMapping("/registration/confirm")
    public String registrationConfirmPageGet(Model model) {
        model.addAttribute("title","Подтверждение аккаунта");
        model.addAttribute("message","Сообщение с кодом отправлено на ваш почтовый адрес");
        return "RegistrationConfirm";
    }

    @PostMapping("/registration/confirm")
    public String registrationConfirmPagePost(String code, Model model) {
        model.addAttribute("title","Подтверждение аккаунта");
        ResetCodes resetCode = resetCodesRepository.searchByResetCode(code);
        if (resetCode != null) {
            User user = resetCode.getUser();
            user.setActive(true);
            usersRepository.save(user);
            resetCodesRepository.delete(resetCode);
            model.addAttribute("message", "Аккаунт успешно активирован");
        }
        else { model.addAttribute("error", "Введённый код недействителен"); }
        return "RegistrationConfirm";
    }

    @GetMapping("/restoration")
    public String restorationPageGet(Model model){
        model.addAttribute("title","Восстановление пароля");
        return "Restoration";
    }

    @PostMapping("/restoration")
    public String restorationPagePost(String email, Model model){
        model.addAttribute("title","Восстановление пароля");
        User user = usersRepository.searchByEmail(email);
        if (user != null) {
            if (user.isActive() == false) { model.addAttribute("error", "Сначала активируйте аккаунт"); return "Restoration"; }
            PasswordGenerator passwordGenerator = new PasswordGenerator.PasswordGeneratorBuilder().useDigits(true).useLower(true).useUpper(true).build();
            String code = passwordGenerator.generate(8);
            String subject = "Восстановление пароля";
            String text = "Ваш код для восстановления: "+code;
            sendEmail(email, subject, text);
            if (resetCodesRepository.searchByUser(user) != null) { resetCodesRepository.delete(resetCodesRepository.searchByUser(user)); }
            ResetCodes resetCode = new ResetCodes();
            resetCode.setResetCode(code);
            resetCode.setUser(user);
            resetCodesRepository.save(resetCode);
            return "redirect:/restoration/confirm";
        }
        else {
            model.addAttribute("error", "Пользователя с таким почтовым адресом нет в системе");
            return "Restoration";
        }
    }

    @GetMapping("/restoration/confirm")
    public String restorationConfirmPageGet(Model model) {
        model.addAttribute("title", "Восстановление пароля");
        model.addAttribute("message", "Код восстановления отправлен на ваш почтовый адрес");
        return "RestorationConfirm";
    }

    @PostMapping("/restoration/confirm")
    public String restorationConfirmPagePost(String code, String password1, String password2, Model model) {
        ResetCodes resetCode = resetCodesRepository.searchByResetCode(code);
        if (code.length() == 8 && password1.equals(password2) && resetCode != null) {
            User user = resetCode.getUser();
            user.setPassword(passwordEncoder.encode(password1));
            usersRepository.save(user);
            resetCodesRepository.delete(resetCode);
            model.addAttribute("message", "Смена пароля успешна");
        }
        else { model.addAttribute("error", "Введённый вами код недействителен или пароли не совпадают"); }
        return "RestorationConfirm";
    }

    @GetMapping("/account")
    public String accountEditGet(Model model){
        model.addAttribute("title","Профиль");
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName().toString());
        model.addAttribute("users", user);
        Iterable<Specializations> specializations = specializationsRepository.findAll();
        model.addAttribute("specializations", specializations);
        if (user.getUserInfo() != null && user.getUserInfo().getImageUrl() != null && user.getUserInfo().getImageUrl() != "") { model.addAttribute("imgSrc", user.getUserInfo().getImageUrl()); }
        else { model.addAttribute("imgSrc", "/uploaded-images/basicavatar.jpg"); }
        if (checkNotification(user) == true) { model.addAttribute("imgNtf","/uploaded-images/ntfunread.jpg"); }
        else { model.addAttribute("imgNtf","/uploaded-images/ntfread.jpg"); }
        if (checkGlobalBan(user) == false) { return "Account"; }
        else { model.addAttribute("message","Вы временно заблокированы"); return "Authorization"; }
    }

    @PostMapping("/account")
    public String accountEditPost(Model model){
        model.addAttribute("title","Профиль");
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName().toString());
        model.addAttribute("users", user);
        if (user.getUserInfo() != null && user.getUserInfo().getImageUrl() != null && user.getUserInfo().getImageUrl() != "") { model.addAttribute("imgSrc", user.getUserInfo().getImageUrl()); }
        else { model.addAttribute("imgSrc", "/uploaded-images/basicavatar.jpg"); }
        if (checkNotification(user) == true) { model.addAttribute("imgNtf","/uploaded-images/ntfunread.jpg"); }
        else { model.addAttribute("imgNtf","/uploaded-images/ntfread.jpg"); }
        if (checkGlobalBan(user) == false) { return "Account"; }
        else { model.addAttribute("message","Вы временно заблокированы"); return "Authorization"; }
    }

    @PostMapping("/account/save")
    public String accountSave(@RequestParam("files") MultipartFile files, String firstname, String middlename, String lastname, Integer course, String specialization, String description, Model model) {
        if (firstname != "" && middlename != "" && lastname != "" && specialization != "" && description != "") {
            User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName().toString());
            PersonalInformation userInfo = user.getUserInfo();
            Specializations spec = specializationsRepository.searchBySpecialization(specialization);
            userInfo.setFirstname(firstname);
            userInfo.setMiddlename(middlename);
            userInfo.setLastname(lastname);
            userInfo.setCourse(course);
            userInfo.setSpecialization(spec);
            userInfo.setDescription(description);
            String uploadDir = "uploaded-images/";
            String fileName = StringUtils.cleanPath(files.getOriginalFilename());
            if (files != null && fileName.equals("") == false) {
                String idName = "user" + String.valueOf(user.getId());
                try {
                    FileUploadService.saveFile(uploadDir, idName+".jpg", files);
                    String imagePath = "/"+uploadDir+idName+".jpg";
                    userInfo.setImageUrl(imagePath);
                }
                catch (IOException e) { System.out.println(e.toString()); throw new RuntimeException(e); }
            }
            personalInformationRepository.save(userInfo);
        }
        return "redirect:/account";
    }

    @PostMapping("/account/change-password")
    public String accountChangePassword(String newPassword, Model model) {
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName().toString());
        if (newPassword.length() >= 8) {
            user.setPassword(passwordEncoder.encode(newPassword));
            usersRepository.save(user);
            model.addAttribute("message","Пароль успешно сменён");
        }
        else { model.addAttribute("message","Длина пароля должна быть не меньше 8 символов"); }
        model.addAttribute("title","Профиль");
        model.addAttribute("users", user);
        if (user.getUserInfo() != null && user.getUserInfo().getImageUrl() != null && user.getUserInfo().getImageUrl() != "") { model.addAttribute("imgSrc", user.getUserInfo().getImageUrl()); }
        else { model.addAttribute("imgSrc", "/uploaded-images/basicavatar.jpg"); }
        if (checkNotification(user) == true) { model.addAttribute("imgNtf","/uploaded-images/ntfunread.jpg"); }
        else { model.addAttribute("imgNtf","/uploaded-images/ntfread.jpg"); }
        if (checkGlobalBan(user) == false) { return "Account"; }
        else { model.addAttribute("message","Вы временно заблокированы"); return "Authorization"; }
    }

    @GetMapping("/users/{id}")
    public String profileGet(@PathVariable(value = "id") long id, Model model) {
        model.addAttribute("title","Пользователь");
        User users = usersRepository.searchById(id);
        model.addAttribute("users", users);
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName().toString());
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]")) { model.addAttribute("globalBan", "Заблокировать"); }
        if (user.getUserInfo() != null && user.getUserInfo().getImageUrl() != null && user.getUserInfo().getImageUrl() != "") { model.addAttribute("imgSrc", user.getUserInfo().getImageUrl()); }
        else { model.addAttribute("imgSrc", "/uploaded-images/basicavatar.jpg"); }
        if (checkNotification(user) == true) { model.addAttribute("imgNtf","/uploaded-images/ntfunread.jpg"); }
        else { model.addAttribute("imgNtf","/uploaded-images/ntfread.jpg"); }
        if (checkGlobalBan(user) == false) { return "UserProfile"; }
        else { model.addAttribute("message","Вы временно заблокированы"); return "Authorization"; }
    }

    @PostMapping("/users/{id}")
    public String profilePost(@PathVariable(value = "id") long id,Model model){
        model.addAttribute("title","Пользователь");
        User users = usersRepository.searchById(id);
        model.addAttribute("users", users);
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName().toString());
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]")) { model.addAttribute("globalBan", "Заблокировать"); }
        if (user.getUserInfo() != null && user.getUserInfo().getImageUrl() != null && user.getUserInfo().getImageUrl() != "") { model.addAttribute("imgSrc", user.getUserInfo().getImageUrl()); }
        else { model.addAttribute("imgSrc", "/uploaded-images/basicavatar.jpg"); }
        if (checkNotification(user) == true) { model.addAttribute("imgNtf","/uploaded-images/ntfunread.jpg"); }
        else { model.addAttribute("imgNtf","/uploaded-images/ntfread.jpg"); }
        if (checkGlobalBan(user) == false) { return "UserProfile"; }
        else { model.addAttribute("message","Вы временно заблокированы"); return "Authorization"; }
    }

    @GetMapping("/users/{id}/global-ban")
    public String globalBanUserPage(@PathVariable(value = "id") long idUser, Model model) {
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName().toString());
        User bannedUser = usersRepository.searchById(idUser);
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]")) {
            model.addAttribute("user", bannedUser);
            if (user.getUserInfo() != null && user.getUserInfo().getImageUrl() != null && user.getUserInfo().getImageUrl() != "") { model.addAttribute("imgSrc", user.getUserInfo().getImageUrl()); }
            else { model.addAttribute("imgSrc", "/uploaded-images/basicavatar.jpg"); }
            if (checkNotification(user) == true) { model.addAttribute("imgNtf","/uploaded-images/ntfunread.jpg"); }
            else { model.addAttribute("imgNtf","/uploaded-images/ntfread.jpg"); }
            if (checkGlobalBan(user) == false) { return "GlobalBan"; }
            else { model.addAttribute("message","Вы временно заблокированы"); return "Authorization"; }
        }
        else { return "redirect:/users/"+String.valueOf(idUser); }
    }

    @PostMapping("/users/{id}/global-ban/apply")
    public String globalBanUserApply(@PathVariable(value = "id") long idUser, boolean banLogIn, boolean banMessageAdd, java.sql.Date banExpireDate, Model model) {
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]")) {
            User bannedUser = usersRepository.searchById(idUser);
            java.util.Date utilDate = new java.util.Date();
            java.sql.Date currentDate = new java.sql.Date(utilDate.getTime());
            if (banExpireDate.after(currentDate)) {
                GlobalBanList globalBan = globalBanListRepository.searchByBannedUser(bannedUser);
                if (globalBan == null) { globalBan = new GlobalBanList(); }
                globalBan.setBannedUser(bannedUser);
                globalBan.setLoginBan(banLogIn);
                globalBan.setWriteBan(banMessageAdd);
                globalBan.setBanStartDate(currentDate);
                globalBan.setBanExpireDate(banExpireDate);
                globalBanListRepository.save(globalBan);
            }
            else { return "redirect:/users/"+String.valueOf(idUser)+"/global-ban"; }
        }
        return "redirect:/users/"+String.valueOf(idUser);
    }

    @GetMapping("/notifications")
    public String notificationsPageGet(Model model) {
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName().toString());
        if (user.getUserInfo() != null && user.getUserInfo().getImageUrl() != null && user.getUserInfo().getImageUrl() != "") { model.addAttribute("imgSrc", user.getUserInfo().getImageUrl()); }
        else { model.addAttribute("imgSrc", "/uploaded-images/basicavatar.jpg"); }
        if (checkNotification(user) == true) { model.addAttribute("imgNtf","/uploaded-images/ntfunread.jpg"); }
        else { model.addAttribute("imgNtf","/uploaded-images/ntfread.jpg"); }
        model.addAttribute("title","Оповещения");
        model.addAttribute("read", notificationsRepository.searchByUserAndNotificationRead(user, true));
        model.addAttribute("unread", notificationsRepository.searchByUserAndNotificationRead(user, false));
        return "Notifications";
    }

    @GetMapping("/notifications/go-to/{id}")
    public String notificationsRedirect(@PathVariable (value = "id") Long id, Model model) {
        Notifications notification = notificationsRepository.searchById(id);
        notification.setNotificationRead(true);
        notificationsRepository.save(notification);
        Messages message = notification.getMessage();
        Threads thread = message.getThread();
        int number = (int) Math.ceil((messagesRepository.searchByThread(thread).indexOf(message)+1.0)/10.0);
        return "redirect:/threads/"+String.valueOf(thread.getId())+"?pageNumber="+String.valueOf(number);
    }

    @GetMapping("/notifications/delete/{id}")
    public String notificationsDelete(@PathVariable (value = "id") Long id, Model model) {
        notificationsRepository.delete(notificationsRepository.searchById(id));
        return "redirect:/notifications";
    }

    @GetMapping("/notifications/clear/read")
    public String notificationsClearRead(Model model) {
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName().toString());
        List<Notifications> notifications = notificationsRepository.searchByUserAndNotificationRead(user, true);
        for (int i = 0; i < notifications.size(); i++) {
            notificationsRepository.delete(notifications.get(i));
        }
        return "redirect:/notifications";
    }

    @GetMapping("/notifications/clear/unread")
    public String notificationsClearUnread(Model model) {
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName().toString());
        List<Notifications> notifications = notificationsRepository.searchByUserAndNotificationRead(user, false);
        for (int i = 0; i < notifications.size(); i++) {
            notificationsRepository.delete(notifications.get(i));
        }
        return "redirect:/notifications";
    }

}
