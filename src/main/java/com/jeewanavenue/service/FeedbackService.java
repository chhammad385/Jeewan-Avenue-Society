package com.jeewanavenue.service;

import com.jeewanavenue.entity.Feedback;
import com.jeewanavenue.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    public List<Feedback> findAll() {
        return feedbackRepository.findAll();
    }

    public Feedback save(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }
}