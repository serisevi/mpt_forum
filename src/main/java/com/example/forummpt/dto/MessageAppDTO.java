package com.example.forummpt.dto;

import com.example.forummpt.models.MessageImages;
import com.example.forummpt.models.Messages;
import com.example.forummpt.repo.MessageImageRepo;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class MessageAppDTO {

        private long id;
        private Long threadId;
        private String username;
        private String userImage;
        private Timestamp messageDatetime;
        private String messageText;
        private String replyText;
        private String replyUsername;
        private String images;
        private String replyImages;

    public MessageAppDTO() {
    }

    public MessageAppDTO(Messages message, MessageImageRepo imagesRepo) {
            this.id = message.getId();
            this.messageText = message.getMessageText();
            this.messageDatetime = message.getMessageDatetime();
            this.threadId = message.getThread().getId();
            this.username = message.getMessageAuthor().getUsername();
            this.userImage = message.getMessageAuthor().getUserInfo().getImageUrl();
            List<MessageImages> imagesList = imagesRepo.searchByMessageOrderById(message);
            String imagesUrls = "";
            for (int i = 0; i < imagesList.size(); i++) {
                if (i > 0) { imagesUrls += ", "; }
                imagesUrls += imagesList.get(i).getImageUrl();
            }
            if (imagesUrls == "") { imagesUrls = null; }
            this.images = imagesUrls;
            Messages reply = message.getMessageReply();
            if (reply != null) {
                this.replyUsername = reply.getMessageAuthor().getUsername();
                this.replyText = reply.getMessageText();
                List<MessageImages> imagesReplyList = imagesRepo.searchByMessageOrderById(reply);
                String imagesReplyUrls = "";
                for (int i = 0; i < imagesReplyList.size(); i++) {
                    if (i > 0) { imagesReplyUrls += ", "; }
                    imagesReplyUrls += imagesReplyList.get(i).getImageUrl();
                }
                if (imagesReplyUrls == "") { imagesReplyUrls = null; }
                this.replyImages = imagesReplyUrls;
            }
            else {
                this.replyText = null;
                this.replyImages = null;
                this.replyUsername = null;
            }
        }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public Timestamp getMessageDatetime() {
        return messageDatetime;
    }

    public void setMessageDatetime(Timestamp messageDatetime) {
        this.messageDatetime = messageDatetime;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getReplyText() {
        return replyText;
    }

    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }

    public String getReplyUsername() {
        return replyUsername;
    }

    public void setReplyUsername(String replyUsername) {
        this.replyUsername = replyUsername;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getReplyImages() {
        return replyImages;
    }

    public void setReplyImages(String replyImages) {
        this.replyImages = replyImages;
    }
}


