package com.sbb01.answer;

import com.sbb01.DataNotFoundException;
import com.sbb01.question.Question;
import com.sbb01.question.QuestionService;
import com.sbb01.user.SiteUser;
import com.sbb01.user.UserService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/answer")
public class AnswerController {

    private final QuestionService questionService;
    private final AnswerService answerService;
    private final UserService userService;

    @PostMapping("/create/{id}")
    @PreAuthorize("isAuthenticated()")
    public String answerCreate(Model model, @PathVariable Integer id, @Valid AnswerForm answerForm, BindingResult bindingResult, Principal principal){
        Question question = this.questionService.getQuestion(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        if(bindingResult.hasErrors()){
            model.addAttribute("question", question);
            return "question_detail";
        }
        Answer answer = this.answerService.create(question, answerForm.getContent(), siteUser);
        return "redirect:/question/detail/%s#answer_%s".formatted(id, answer.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String answerModify(AnswerForm answerForm, @PathVariable("id") int id, Principal principal){
        Answer answer = this.answerService.getAnswer(id);
        if(!answer.getAuthor().getUsername().equals(principal.getName())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"수정권한이 없습니다.");
        }
        answerForm.setContent(answer.getContent());
        return "answer_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String answerModify(@Valid AnswerForm answerForm, @PathVariable("id") int id, BindingResult bindingResult,
                               Principal principal){
        if(bindingResult.hasErrors()){
            return "answer_form";
        }
        Answer answer = this.answerService.getAnswer(id);
        if(!answer.getAuthor().getUsername().equals(principal.getName())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.answerService.modify(answer, answerForm.getContent());
        return "redirect:/question/detail/%s#answer_%s".formatted(answer.getQuestion().getId(), answer.getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String answerDelete(AnswerForm answerForm, @PathVariable("id") int id, Principal principal){
        Answer answer = this.answerService.getAnswer(id);
        if(!answer.getAuthor().getUsername().equals(principal.getName())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"삭제권한이 없습니다.");
        }
        this.answerService.delete(answer);
        return "redirect:/question/detail/%s".formatted(answer.getQuestion().getId());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String answerVote(Principal principal, @PathVariable("id") int id){
        Answer answer = this.answerService.getAnswer(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.answerService.vote(answer, siteUser);
        return "redirect:/question/detail/%s#answwer_%s".formatted(answer.getQuestion().getId(), answer.getId());
    }


}
