package com.bmt.MyStore.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"","/"})
    public String home(){

        return "index";
    }

    @GetMapping({"/contact"})
    public String contact(){

        return "contact";
    }

    @GetMapping({"/privacy"})
    public String privacy(){

        return "privacy";
    }


}
