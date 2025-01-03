package com.smart.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smart.dao.UserDao;
import com.smart.entities.User;

@Service
public class UserService {
	
	@Autowired
	UserDao userdao;
	
	public User addUser(User user)
	{
		return userdao.save(user);
	}

	public User getUserByEmail(String email)
	{
		return userdao.getUserByEmail(email);
	}
}
