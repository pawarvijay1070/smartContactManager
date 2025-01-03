package com.smart.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.smart.dao.ContactDao;
import com.smart.entities.Contact;
import com.smart.entities.User;

@Service
public class ContactService {

	@Autowired
	private ContactDao contactDao;
	
	public Contact addContact(Contact contact)
	{
		return contactDao.save(contact);
	}
	
	public Page<Contact> getContactByUserId(int userId, Pageable pageable)
	{
		return contactDao.getContactByUserid(userId, pageable);
	}
	
	public Optional<Contact> findById(int cId)
	{
		 return contactDao.findById(cId);
	}
	
	public void deleteById(int cId)
	{
		contactDao.deleteById(cId);
	}
}
