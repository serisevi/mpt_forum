package com.example.forummpt.dto;

import com.example.forummpt.models.MessageImages;

public class MessageImagesDTO {

    private long id;
    private Long messageId;
    private String imageUrl;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public MessageImagesDTO(MessageImages messageImages) {
        this.id = messageImages.getId();
        this.imageUrl = messageImages.getImageUrl();
        this.messageId = messageImages.getMessage().getId();
    }

}
