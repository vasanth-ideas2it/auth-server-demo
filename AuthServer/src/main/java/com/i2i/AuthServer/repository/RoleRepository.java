package com.i2i.AuthServer.repository;

import com.i2i.AuthServer.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
@RestController
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}