package com.mycompany.heromarsspring.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.mycompany.heromarsspring.services.SessionService;

@Controller
public class MainPageController {
	
@Autowired
SessionService sessionService;
	
	@RequestMapping(value = "index", method = RequestMethod.GET)
	public String showMainMenu(Model model) {

		model.addAttribute("loggedInUserName", sessionService.getCurrentUserName());
		
		return "index.html";
	}
	
	@RequestMapping(value = "stb", method = RequestMethod.GET)
	public String showStb(Model model) {
		
		model.addAttribute("loggedInUserName", sessionService.getCurrentUserName());

		return "stb.html";
	}

}
