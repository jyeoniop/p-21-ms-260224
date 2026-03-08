package com.sbb01.question;

import com.sbb01.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    public List<Question> getList(){
        return this.questionRepository.findAll();
    }

    public Question getQuestion(Integer id){
        Optional<Question> q = this.questionRepository.findById(id);
        if(q.isPresent()){
            return q.get();
        }else{
            throw new DataNotFoundException("qestion not found");
        }
    }

    public void create(String subject, String content){
        Question q = new Question();
        q.setContent(content);
        q.setSubject(subject);
        q.setCreateDate(LocalDateTime.now());
        this.questionRepository.save(q);
    }



}
