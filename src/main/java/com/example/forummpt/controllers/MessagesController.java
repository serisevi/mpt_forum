package com.example.forummpt.controllers;

import com.example.forummpt.models.*;
import com.example.forummpt.repo.*;
import com.example.forummpt.services.FileUploadService;
import com.example.forummpt.services.MessagesService;
import com.example.forummpt.services.ThreadsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

@Controller
public class MessagesController {

    private final MessagesService messagesService;

    @Autowired
    private LocalBanListRepo localBanListRepository;
    @Autowired
    private MessageImageRepo messageImageRepository;
    @Autowired
    private ThreadsRepo threadsRepository;
    @Autowired
    private MessagesRepo messagesRepository;
    @Autowired
    private UsersRepo usersRepository;
    @Autowired
    private GlobalBanListRepo globalBanListRepository;
    @Autowired
    private NotificationsRepo notificationsRepository;
    @Autowired
    public MessagesController(MessagesService messagesService) {
        this.messagesService = messagesService;
    }

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

    @GetMapping("/threads/{id}")
    public String threadGet(@PathVariable(value = "id") long id, @RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
                            @RequestParam(value = "size", required = false, defaultValue = "10") int size, Model model) {
        model.addAttribute("title","Обсуждение");
        Threads thread = threadsRepository.searchById(id);
        model.addAttribute("threadId", thread.getId());
        model.addAttribute("threadName", thread.getThreadName().toString());
        model.addAttribute("threadDesc", thread.getThreadDescription());
        model.addAttribute("messages", messagesService.getPage(pageNumber, size, thread));
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName().toString());
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]")) {
            model.addAttribute("delete", "Удалить");
        }
        if (thread.getThreadAuthor() == user || SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]")) {
            model.addAttribute("banOnThread","Исключить");
            model.addAttribute("banListLink", "Список блокировок");
        }
        if (user.getUserInfo() != null && user.getUserInfo().getImageUrl() != null && user.getUserInfo().getImageUrl() != "") { model.addAttribute("imgSrc", user.getUserInfo().getImageUrl()); }
        else { model.addAttribute("imgSrc", "/uploaded-images/basicavatar.jpg"); }
        if (checkNotification(user) == true) { model.addAttribute("imgNtf","/uploaded-images/ntfunread.jpg"); }
        else { model.addAttribute("imgNtf","/uploaded-images/ntfread.jpg"); }
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]")) { model.addAttribute("delete", "Удалить"); }
        if (checkGlobalBan(user) == false) { return "MessagesByPage"; }
        else { model.addAttribute("message","Вы временно заблокированы"); return "Authorization"; }
    }

    @PostMapping("/threads/{id}")
    public String threadPost(@PathVariable(value = "id") long id, @RequestParam(value = "pageNumber", required = false, defaultValue = "1") int pageNumber,
                             @RequestParam(value = "size", required = false, defaultValue = "10") int size, Model model) {
        model.addAttribute("title","Обсуждение");
        Threads thread = threadsRepository.searchById(id);
        model.addAttribute("threadId", thread.getId());
        model.addAttribute("threadName", thread.getThreadName().toString());
        model.addAttribute("threadDesc", thread.getThreadDescription());
        model.addAttribute("messages", messagesService.getPage(pageNumber, size, thread));
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName().toString());
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]")) {
            model.addAttribute("delete", "Удалить");
        }
        if (thread.getThreadAuthor() == user || SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]")) {
            model.addAttribute("banOnThread","Исключить");
            model.addAttribute("banListLink", "Список блокировок");
        }
        if (user.getUserInfo() != null && user.getUserInfo().getImageUrl() != null && user.getUserInfo().getImageUrl() != "") { model.addAttribute("imgSrc", user.getUserInfo().getImageUrl()); }
        else { model.addAttribute("imgSrc", "/uploaded-images/basicavatar.jpg"); }
        if (checkNotification(user) == true) { model.addAttribute("imgNtf","/uploaded-images/ntfunread.jpg"); }
        else { model.addAttribute("imgNtf","/uploaded-images/ntfread.jpg"); }
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]")) { model.addAttribute("delete", "Удалить"); }
        if (checkGlobalBan(user) == false) { return "Thread"; }
        else { model.addAttribute("message","Вы временно заблокированы"); return "Authorization"; }
    }

    @PostMapping("threads/{id}/add")
    public String messageAdd(@PathVariable(value = "id") long id, @RequestParam("files") MultipartFile[] files, String text, Integer replyId, Model model) {
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        GlobalBanList globalBan = globalBanListRepository.searchByBannedUser(user);
        java.sql.Date currentDate = new java.sql.Date(new java.util.Date().getTime());
        Threads thread = threadsRepository.searchById(id);
        Messages message = new Messages();
        message.setMessageText(text);
        message.setMessageAuthor(user);
        message.setThread(thread);
        message.setMessageDatetime(new Timestamp(System.currentTimeMillis()));
        message.setMessageReply(null);
        messagesRepository.save(message);
        LocalBanList localBan = localBanListRepository.searchByBanThreadAndBannedUser(thread, user);
        if (text.equals("") == false && text != null && localBan == null) {
            if (globalBan == null || currentDate.after(globalBan.getBanExpireDate())) {
                messagesRepository.save(message);
                if (replyId != null) {
                    message.setMessageReply(messagesRepository.searchById(Long.valueOf(replyId)));
                    messagesRepository.save(message);
                    Notifications notification = new Notifications();
                    notification.setText("На ваше сообщение ответил "+message.getMessageAuthor().getUsername());
                    notification.setNotificationRead(false);
                    notification.setMessage(message);
                    notification.setUser(message.getMessageReply().getMessageAuthor());
                    notificationsRepository.save(notification);
                }
                try {
                    String uploadDir = "uploaded-images/";
                    if (files != null) {
                        for (int i = 0; i < files.length; i++) {
                            String fileName = StringUtils.cleanPath(files[i].getOriginalFilename());
                            if (fileName.equals("") == true) { break; }
                            else {
                                int idName = -1;
                                List<MessageImages> list = messageImageRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
                                if (list.size() != 0) {
                                    idName = ((int)list.get(0).getId())+1;
                                }
                                else { idName = 1; }
                                idName++;
                                FileUploadService.saveFile(uploadDir, String.valueOf(idName)+".jpg", files[i]);
                                String imagePath = "/"+uploadDir+idName+".jpg";
                                MessageImages messageImages = new MessageImages();
                                messageImages.setMessage(message);
                                messageImages.setImageUrl(imagePath);
                                messageImageRepository.save(messageImages);
                            }
                            if (i == 2) { break; }
                        }
                    }
                } catch (Exception e) { System.out.println(e.toString()); throw new RuntimeException(e); }
                int number = (int) Math.ceil((messagesRepository.searchByThread(thread).indexOf(message)+1.0)/10.0);
                return "redirect:/threads/"+String.valueOf(id)+"?pageNumber="+String.valueOf(number);
            }
        }
        return "redirect:/threads/"+String.valueOf(id)+"?pageNumber=1";
    }

    @PostMapping("threads/{id}/search")
    public String messagesSearch(@PathVariable(value = "id") long id, String text, Model model) {
        model.addAttribute("title","Обсуждение");
        Threads thread = threadsRepository.searchById(id);
        model.addAttribute("threadId", thread.getId());
        model.addAttribute("threadName", thread.getThreadName().toString());
        model.addAttribute("threadDesc", thread.getThreadDescription());
        List<Messages> messages = messagesRepository.searchByThreadAndMessageTextContains(thread, text);
        model.addAttribute("messages", messages);
        User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName().toString());
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]")) {
            model.addAttribute("delete", "Удалить");
        }
        if (thread.getThreadAuthor() == user || SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]")) {
            model.addAttribute("banOnThread","Исключить");
            model.addAttribute("banListLink", "Список блокировок");
        }
        if (user.getUserInfo() != null && user.getUserInfo().getImageUrl() != null && user.getUserInfo().getImageUrl() != "") { model.addAttribute("imgSrc", user.getUserInfo().getImageUrl()); }
        else { model.addAttribute("imgSrc", "/uploaded-images/basicavatar.jpg"); }
        if (checkNotification(user) == true) { model.addAttribute("imgNtf","/uploaded-images/ntfunread.jpg"); }
        else { model.addAttribute("imgNtf","/uploaded-images/ntfread.jpg"); }
        if (checkGlobalBan(user) == false) { return "Thread"; }
        else { model.addAttribute("message","Вы временно заблокированы"); return "Authorization"; }
    }

    @GetMapping("/threads/{id}/delete/{id2}")
    public String messageDelete(@PathVariable(value = "id") long idThread, @PathVariable(value = "id2") long idMessage, Model model) {
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]")) {
            Messages message = messagesRepository.searchById(idMessage);
            message.setMessageText("Сообщение было удалено.");
            messagesRepository.save(message);
        }
        return "redirect:/threads/"+String.valueOf(idThread);
    }

    @GetMapping("/threads/{id}/ban-user/{id2}")
    public String threadLocalBan(@PathVariable(value = "id") long idThread, @PathVariable(value = "id2") long idUser, Model model) {
        User loggedIn = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        User bannedUser = usersRepository.searchById(idUser);
        Threads thread = threadsRepository.searchById(idThread);
        if ((SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]") || loggedIn == thread.getThreadAuthor())
                && loggedIn != bannedUser && localBanListRepository.searchByBanThreadAndBannedUser(thread, bannedUser) == null && bannedUser != thread.getThreadAuthor()) {
            LocalBanList localBan = new LocalBanList();
            localBan.setBanThread(threadsRepository.searchById(idThread));
            localBan.setBannedUser(usersRepository.searchById(idUser));
            localBanListRepository.save(localBan);
        }
        return "redirect:/threads/"+String.valueOf(idThread);
    }

    @GetMapping("/threads/{id}/ban-list")
    public String threadBanList(@PathVariable(value = "id") long idThread, Model model) {
        if ((SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]") ||
                usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName()) == threadsRepository.searchById(idThread).getThreadAuthor())) {
            User user = usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName().toString());
            model.addAttribute("thread", threadsRepository.searchById(idThread));
            Iterable<LocalBanList> bans = localBanListRepository.searchByBanThread(threadsRepository.searchById(idThread));
            model.addAttribute("bans", bans);
            if (user.getUserInfo() != null && user.getUserInfo().getImageUrl() != null && user.getUserInfo().getImageUrl() != "") { model.addAttribute("imgSrc", user.getUserInfo().getImageUrl()); }
            else { model.addAttribute("imgSrc", "/uploaded-images/basicavatar.jpg"); }
            if (checkNotification(user) == true) { model.addAttribute("imgNtf","/uploaded-images/ntfunread.jpg"); }
            else { model.addAttribute("imgNtf","/uploaded-images/ntfread.jpg"); }
            if (checkGlobalBan(user) == false) { return "ThreadBanList"; }
            else { model.addAttribute("message","Вы временно заблокированы"); return "Authorization"; }
        }
        else { return "redirect:/threads/"+String.valueOf(idThread); }
    }

    @PostMapping("/threads/{id}/unban/{id2}")
    public String threadUnban(@PathVariable(value = "id") long idThread, @PathVariable(value = "id2") long idUser, Model model) {
        if ((SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString().contains("[ADMIN]") ||
                usersRepository.searchByUsername(SecurityContextHolder.getContext().getAuthentication().getName()) == threadsRepository.searchById(idThread).getThreadAuthor())) {
            Threads thread = threadsRepository.searchById(idThread);
            User user = usersRepository.searchById(idUser);
            localBanListRepository.delete(localBanListRepository.searchByBanThreadAndBannedUser(thread, user));
        }
        return "redirect:/threads/"+String.valueOf(idThread)+"/ban-list";
    }

}
