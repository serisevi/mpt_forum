package com.example.forummpt.models;

import com.example.forummpt.dto.MessageImagesDTO;
import com.example.forummpt.repo.MessageImageRepo;
import com.example.forummpt.repo.MessagesRepo;
import org.springframework.web.bind.annotation.Mapping;

import javax.persistence.*;

@Entity
public class MessageImages {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Messages message;
    private String imageUrl;

    public MessageImages() {}

    public MessageImages(MessageImagesDTO dto, MessagesRepo messagesRepo) {
        this.id = dto.getId();
        this.imageUrl = dto.getImageUrl();
        this.message = messagesRepo.searchById(dto.getMessageId());
    }

    public MessageImages(long id, Messages message, String imageUrl) {
        this.id = id;
        this.message = message;
        this.imageUrl = imageUrl;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public Messages getMessage() {
        return message;
    }
    public void setMessage(Messages message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
