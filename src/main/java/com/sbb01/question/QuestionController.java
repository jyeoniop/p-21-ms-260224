package com.sbb01.question;

import com.sbb01.answer.AnswerForm;
import com.sbb01.user.SiteUser;
import com.sbb01.user.UserService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.introspect.Annotated;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/question")
public class QuestionController {
    private final QuestionService questionService;
    private final UserService userService;

    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(value="page", defaultValue = "0") int page,
                       @RequestParam(value = "kw", defaultValue = "") String kw){
        Page<Question> paging = this.questionService.getList(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);

        return "question_list";
    }

    @GetMapping("/detail/{id}")
    public String detail(Model model, @PathVariable Integer id, AnswerForm answerForm){
        Question q = this.questionService.getQuestion(id);
        model.addAttribute("question", q);
        return "question_detail";
    }

    @GetMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public String questionCreate(QuestionForm questionForm){
        return "question_form";
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public String questionCreate(@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal){
        SiteUser siteUser = this.userService.getUser(principal.getName());
        if(bindingResult.hasErrors()) {
            return "question_form";
        }
        this.questionService.create(questionForm.getSubject(), questionForm.getContent(),siteUser);
        return "redirect:/question/list";
    }

    @GetMapping("/modify/{id}")
    @PreAuthorize("isAuthenticated()")
    public String questionModify(QuestionForm questionForm, @PathVariable("id") int id, Principal principal){
        Question question = questionService.getQuestion(id);
        if(!question.getAuthor().getUsername().equals(principal.getName())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        questionForm.setSubject(question.getSubject());
        questionForm.setContent(question.getContent());
        return "question_form";
    }

    @PostMapping("/modify/{id}")
    @PreAuthorize("isAuthenticated()")
    public String questionModify(@Valid QuestionForm questionForm, BindingResult bindingResult, Principal principal,
                                 @PathVariable("id") int id){
        if(bindingResult.hasErrors()){
            return "question_form";
        }
        Question question = this.questionService.getQuestion(id);
        if(!question.getAuthor().getUsername().equals(principal.getName())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }
        this.questionService.modify(question, questionForm.getSubject(), questionForm.getContent());
        return "redirect:/question/detail/%s".formatted(id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String questionDelete(QuestionForm questionForm, Principal principal, @PathVariable("id") int id){
        Question question = this.questionService.getQuestion(id);
        if(!question.getAuthor().getUsername().equals(principal.getName())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }
        this.questionService.delete(question);
        return "redirect:/";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String questionVote(Principal principal, @PathVariable("id") int id){
        Question question = this.questionService.getQuestion(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.questionService.vote(question, siteUser);
        return "redirect:/question/detail/%s".formatted(id);
    }



}
