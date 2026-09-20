package com.txy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by huyawei 2022/11/8 下午3:56
 */

@Controller
@RequestMapping("/")
public class FossaController {

    @RequestMapping
    public String login(ModelMap model, HttpServletRequest request){
        return "index";
    }

}
