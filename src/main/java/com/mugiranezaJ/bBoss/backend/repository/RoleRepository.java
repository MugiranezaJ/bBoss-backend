package com.mugiranezaJ.bBoss.backend.repository;

import com.mugiranezaJ.bBoss.backend.model.Role;
import com.mugiranezaJ.bBoss.backend.model.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
}
