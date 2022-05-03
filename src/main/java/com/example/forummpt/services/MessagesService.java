package com.example.forummpt.services;

import com.example.forummpt.models.Messages;
import com.example.forummpt.models.Threads;
import com.example.forummpt.pagination.Paged;
import com.example.forummpt.pagination.Paging;
import com.example.forummpt.repo.MessagesRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class MessagesService {

    private final MessagesRepo messagesRepository;

    @Autowired
    public MessagesService(MessagesRepo messagesRepository) {
        this.messagesRepository = messagesRepository;
    }

    public Paged<Messages> getPage(int pageNumber, int size, Threads thread) {
        PageRequest request = PageRequest.of(pageNumber - 1, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<Messages> messagesPage = messagesRepository.findMessagesByThread(thread, request);
        return new Paged<>(messagesPage, Paging.of(messagesPage.getTotalPages(), pageNumber, size));
    }

}
