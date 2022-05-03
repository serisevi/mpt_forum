package com.example.forummpt.services;

import com.example.forummpt.models.Threads;
import com.example.forummpt.pagination.Paged;
import com.example.forummpt.pagination.Paging;
import com.example.forummpt.repo.ThreadsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class ThreadsService {

    private final ThreadsRepo threadsRepository;

    @Autowired
    public ThreadsService(ThreadsRepo threadsRepository) {
        this.threadsRepository = threadsRepository;
    }

    public Paged<Threads> getPage(int pageNumber, int size) {
        PageRequest request = PageRequest.of(pageNumber - 1, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<Threads> threadsPage = threadsRepository.findAll(request);
        return new Paged<>(threadsPage, Paging.of(threadsPage.getTotalPages(), pageNumber, size));
    }

}
