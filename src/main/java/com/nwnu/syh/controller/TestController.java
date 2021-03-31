package com.nwnu.syh.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description: *
 * @author: 司云航
 * @create: 2020-03-29 18:10
 */
@Controller
public class TestController {
    @RequestMapping("/test.do")
    @ResponseBody
    public String toTest(Model model){
//        model.addAttribute("name","张三");
        return "test";
    }
}
