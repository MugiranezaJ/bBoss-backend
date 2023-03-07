package com.mugiranezaJ.bBoss.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.mugiranezaJ.bBoss.backend.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional findByEmail(String email);
    Boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

}
