package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder encoder;
	//private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

	@Autowired
	private UserService userService;

	@PostMapping("/deleteSessionMessage")
	public void deleteSessionMessage(HttpSession session)
	{
		if (session.getAttribute("message") != null)
		{
				session.removeAttribute("message");
		}
	}
	
	@GetMapping("/")
	public String home(Model model)
	{
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}

	@GetMapping("/signin")
	public String login(Model model)
	{
		model.addAttribute("title", "Login - Smart Contact Manager");
		return "login";
	}

	@GetMapping("/about")
	public String about(Model model)
	{
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}
	
	@GetMapping("/signup")
	public String signup(Model model, HttpSession session)
	{
		if (session.getAttribute("message") != null)
		{
			session.removeAttribute("message");
		}

		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}
	
	@GetMapping("doRegister")
	public String doRegisterGet()
	{
		return "redirect:/signup";
	}
	
	
	@PostMapping("/doRegister")
	public String doRegister(@Valid @ModelAttribute("user") User user, BindingResult result, @RequestParam(value="agreement", defaultValue = "false") boolean agreement, Model model, HttpSession session, HttpServletResponse response)
	{
		try
		{
			if(!agreement)
			{
				throw new Exception("Accept terms and condition");
			}
			
			if (result.hasErrors())
			{
				//System.out.println(result.getAllErrors());
				model.addAttribute("user", user);
				return "signup";
			}
			
			user.setEnabled(true);
			user.setRole("ROLE_USER");
			user.setImageUrl("Default.png");
			user.setPassword(this.encoder.encode(user.getPassword()));
			userService.addUser(user);
			
			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Registered succesfully", "alert-success"));
			
			//response.sendRedirect("signup");
			return "/signup";
		}
		catch (Exception e)
		{
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message(e.getMessage(), "alert-danger"));
			return "/signup";
		}
		

	}
}
