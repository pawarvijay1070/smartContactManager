package com.smart.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smart.entities.User;

@Repository
public interface UserDao extends JpaRepository<User, Integer> {

	@Query("select u from User u where u.email = :email")
	User getUserByEmail(@Param("email") String email);
}
