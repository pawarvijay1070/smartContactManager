package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.ContactService;
import com.smart.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;
	
	@Autowired
	private ContactService contactService;
	
	@ModelAttribute
	public void addCommonData(Model model, Principal principal)
	{
		String email = principal.getName();
		User user = userService.getUserByEmail(email);
		model.addAttribute("user", user);
	}
	
	@GetMapping("/dashboard")
	public String dashboard(Model model, Principal principal)
	{
		model.addAttribute("title", "User Dashboard");
		return "user/dashboard";
	}
	
	@GetMapping("/profile")
	public String profile(Model model)
	{
		model.addAttribute("title", "User Profile");
		return "user/profile";
	}

	@PostMapping("/editUser")
	public String editUser(Model model)
	{
		model.addAttribute("title", "Update Profile");
		return "user/updateProfile";
	}
	
	@PostMapping("/processEditUser")
	public String processEditUser(@Valid @ModelAttribute("user") User user, BindingResult result, @RequestParam("contactImage") MultipartFile file, Model model, Principal principal, HttpSession session)
	{
		try
		{
			
			if(result.hasErrors())
			{
				model.addAttribute("user", user);
				return "user/updateProfile";
			}
			
			if(file.isEmpty())
			{
				user.setImageUrl(user.getImageUrl());
			}
			else
			{
				File saveFile = new ClassPathResource("static/img").getFile();
				
				String imgName = "";
				if (user.getImageUrl().equals("Default.png"))
				{
					System.out.println("Profile photo changed from default photo");
					String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
					imgName = "contact_"+ UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + extension;
					user.setImageUrl(imgName);
					
				}
				else
				{
					System.out.println("Profile photo was changed");
					imgName = user.getImageUrl();
				}
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+imgName);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
			}
	
			this.userService.addUser(user);
			session.setAttribute("message", new Message("Profile updated succesfully", "alert-success"));
			
			//return "normal/processContact";
			return "redirect:/user/profile";
		}
		catch (Exception e)
		{
			model.addAttribute("user", user);
			session.setAttribute("message", new Message(e.getMessage(), "alert-danger"));
			return "user/updateProfile";
			
		}
	}
	
	@GetMapping("/contacts/{page}")
	public String contacts(@PathVariable("page") int page, Model model, Principal principal, HttpSession session)
	{
		if (session.getAttribute("message") != null)
		{
			model.addAttribute("message", session.getAttribute("message"));
			session.removeAttribute("message");
		}
		
		model.addAttribute("title", "Contacts List");
		
		String email = principal.getName();
		User user = userService.getUserByEmail(email);
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contactList = contactService.getContactByUserId(user.getId(), pageable);

		model.addAttribute("contactList", contactList);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contactList.getTotalPages());
		return "user/contacts";
	}
	
	@GetMapping("/contact/{cId}")
	public String showContactDetails(@PathVariable("cId") int cId, Model model, Principal principal, HttpSession session)
	{
		if (session.getAttribute("message") != null)
		{
			model.addAttribute("message", session.getAttribute("message"));
			session.removeAttribute("message");
		}

		model.addAttribute("title", "Contact Details");
		
		Optional<Contact> contactOptional = this.contactService.findById(cId);
		Contact contact = contactOptional.get();
		
		String email = principal.getName();
		User user = userService.getUserByEmail(email);
		
		if(contact.getUser().getId() == user.getId())
			model.addAttribute("contact", contact);
		
		return "user/contactDetails";
	}
	
	@GetMapping("/editContact/{cId}")
	public String editContact(@PathVariable("cId") int cId, Model model, Principal principal)
	{
		model.addAttribute("title", "Edit Contact - Smart Contact Manager");
		
		Optional<Contact> contactOptional = this.contactService.findById(cId);
		Contact contact = contactOptional.get();
		
		String email = principal.getName();
		User user = userService.getUserByEmail(email);
		
		if(contact.getUser().getId() == user.getId())
		{
			model.addAttribute("contact", contact);
			model.addAttribute("cId", cId);
		}
		
		return "user/editContact";
	}
	
	@PostMapping("/processEditContact")
	public String processEditContact(@Valid @ModelAttribute("contact") Contact contact, BindingResult result, @RequestParam("contactImage") MultipartFile file, Model model, Principal principal, HttpSession session)
	{
		
		try
		{
			String email = principal.getName();
			User user = userService.getUserByEmail(email);
			contact.setUser(user);
			
			if(result.hasErrors())
			{
				model.addAttribute("contact", contact);
				return "user/editContact";
			}
			
			if(file.isEmpty())
			{
				System.out.println("Profile photo was not changed");
				contact.setImageUrl(contact.getImageUrl());
			}
			else
			{
				File saveFile = null;
				String imgName = "";
				if (contact.getImageUrl().equals("Profile-photo.jpg"))
				{
					String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
					imgName = "contact_"+ UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + extension;
					contact.setImageUrl(imgName);
					
				}
				else
				{
					imgName = contact.getImageUrl();
				}

				saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+imgName);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
			}
	
			this.contactService.addContact(contact);
			model.addAttribute("contact", new Contact());
			session.setAttribute("message", new Message("Contact updated succesfully", "alert-success"));
			
			//return "normal/processContact";
			return "redirect:/user/contact/"+contact.getcId();
		}
		catch (Exception e)
		{
			model.addAttribute("contact", contact);
			session.setAttribute("message", new Message(e.getMessage(), "alert-danger"));
			return "user/editContact";
			
		}
	}
	
	@GetMapping("/deleteContact/{cId}")
	public String deleteContact(@PathVariable("cId") int cId, Model model, Principal principal, HttpSession session)
	{
		Optional<Contact> contactOptional = this.contactService.findById(cId);	
		Contact contact = contactOptional.get();
		
		String email = principal.getName();
		User user = userService.getUserByEmail(email);
		
		
		if(contact.getUser().getId() == user.getId())
		{
			if(! contact.getImageUrl().equals("Profile-photo.jpg"))
			{
				try
				{	
					File saveFile = new ClassPathResource("static/img").getFile();
					
					Path path = Paths.get(saveFile.getAbsolutePath()+File.separator + contact.getImageUrl());
					
					//Path path = Paths.get("static/img/"+contact.getImageUrl());
					if (Files.exists(path))
					{
						Files.delete(path); // Deletes the file
		                System.out.println("File deleted successfully");
					}
				}
				catch (Exception e) 
				{
					System.err.println("Failed to delete file");
		            e.printStackTrace();
				}
			}
//			contact.setUser(null);
			contactService.deleteById(cId);
			session.setAttribute("message", new Message("Contact deleted successfully.!", "alert-success"));
		}
		
		return "redirect:/user/contacts/0";
			
	}
	
	@GetMapping("/addContact")
	public String addContact(Model model, HttpSession session)
	{
		if (session.getAttribute("message") != null)
		{
			session.removeAttribute("message");
		}
		model.addAttribute("title", "Add Contact - Smart Contact Manager");
		model.addAttribute("contact", new Contact());
		return "user/addContact";
	}
	
	@PostMapping("/processContact")
	public String processContact(@Valid @ModelAttribute("contact") Contact contact, BindingResult result, @RequestParam("contactImage") MultipartFile file, Model model, HttpSession sessions, Principal principal, HttpSession session)
	{
		model.addAttribute("title", "Add Contact - Smart Contact Manager");
		
		try
		{
			if(result.hasErrors())
			{
				model.addAttribute("contact", contact);
				return "user/addContact";
			}
			
			if(file.isEmpty())
			{
				contact.setImageUrl("Profile-photo.jpg");
			}
			else
			{
				String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
				String imgName = "contact_"+ UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + extension;
				contact.setImageUrl(imgName);
				
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+imgName);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
			}
	
			String email = principal.getName();
			User user = userService.getUserByEmail(email);
			user.getContacts().add(contact);
			
			contact.setUser(user);
			this.contactService.addContact(contact);
			model.addAttribute("contact", new Contact());
			session.setAttribute("message", new Message("Contact added succesfully", "alert-success"));
			
			//return "normal/processContact";
			return "user/addContact";
		}
		catch (Exception e)
		{
			model.addAttribute("contact", contact);
			session.setAttribute("message", new Message(e.getMessage(), "alert-danger"));
			return "user/addContact";
			
		}
	}
}

//"~{user/base::layout(~{::section})}" 
//"~{user/base::layout(~{::section})}"
