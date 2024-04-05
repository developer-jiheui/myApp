package com.gdu.myapp.controller;

import com.gdu.myapp.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/blog")
@RequiredArgsConstructor
@Controller
public class BlogController {

    private final BlogService blogService;

    @GetMapping("/list.do")
    public String blogList(){

        return "blog/list";
    }

    @GetMapping("/write.page")
    public String writePage(){
        return "blog/write";
    }

}
