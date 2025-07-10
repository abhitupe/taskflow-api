package com.taskflow.api.repository;

import com.taskflow.api.model.User;
import com.taskflow.api.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    List<User> findByIsActiveTrue();

    List<User> findByRole(Role role);

}
