package com.mugiranezaJ.bBoss.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mugiranezaJ.bBoss.backend.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional findByEmail(String email);

}
