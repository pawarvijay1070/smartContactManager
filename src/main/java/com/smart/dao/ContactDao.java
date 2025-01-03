package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smart.entities.Contact;

@Repository
public interface ContactDao extends JpaRepository<Contact, Integer> {
	
	@Query("select c from Contact c where c.user.id = :userId")
	Page<Contact> getContactByUserid(@Param("userId") int userId, Pageable pageable);
}
