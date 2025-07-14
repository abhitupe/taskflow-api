package com.taskflow.api.service;


import com.taskflow.api.exception.ResourceNotFoundException;
import com.taskflow.api.model.User;
import com.taskflow.api.model.enums.Role;
import com.taskflow.api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(User user) throws BadRequestException {
        log.info("Attempting to register new user: {}", user.getUsername());

        if (userRepository.existsByUsername(user.getUsername())) {
            log.warn("Registration failed: Username '{}' already exists", user.getUsername());
            throw new BadRequestException("Username already exists: " + user.getUsername());
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("Registration failed: Email '{}' already exists", user.getEmail());
            throw new BadRequestException("Email already exists: " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRole() == null){
            user.setRole(Role.DEVELOPER);
        }

        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }

        User savedUser = userRepository.save(user);
        log.info("Successfully registered user: '{}' with ID: '{}'", savedUser.getUsername(), savedUser.getId());

        return savedUser;

    }

    @Transactional(readOnly = true)
    public  User findByUsername(String username) {

        log.debug("Finding user by username: '{}'", username);

        return  userRepository.findByUsername(username).orElseThrow(() -> {
            log.warn("User with username '{}' not found", username);
            return new ResourceNotFoundException("User with username '" + username + "' not found");
        });

    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {

        log.debug("Finding user by email: '{}'", email);

        return userRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("User with email '{}' not found", email);
            return new ResourceNotFoundException("User with email '" + email + "' not found");
        });

    }

    @Transactional(readOnly = true)
    public User findById(Long id) {

        log.debug("Finding user by ID: '{}'", id);

        return userRepository.findById(id).orElseThrow(() -> {
            log.warn("User with ID '{}' not found", id);
            return new ResourceNotFoundException("User with ID '" + id + "' not found");
        });

    }

    @Transactional(readOnly = true)
    public List<User> findAllActiveUsers() {

        log.debug("Finding all active users");
        return userRepository.findByIsActiveTrue();

    }

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {

        log.debug("Finding all users");
        return userRepository.findAll();

    }

    public User updateUserProfile(Long userId, User updatedUser) throws BadRequestException {

        log.info("Updating profile for user ID: {}", userId);

        User existingUser = findById(userId);

        if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
            if (userRepository.existsByEmail(updatedUser.getEmail())) {
                log.warn("Profile update failed: Email '{}' already exists", updatedUser.getEmail());
                throw new BadRequestException("Email already exists: " + updatedUser.getEmail());
            }
        }

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());

        User savedUser = userRepository.save(existingUser);
        log.info("Successfully updated profile for user: '{}'", savedUser.getUsername());

        return savedUser;

    }

    public User updateUserPassword(Long userId, String newPassword) {

        log.info("Updating password for user: {}", userId);

        User user = findById(userId);
        user.setPassword(passwordEncoder.encode(newPassword));

        User savedUser = userRepository.save(user);
        log.info("Successfully updated password for user: {}", savedUser.getUsername());

        return savedUser;

    }

    public User updateUserRole(Long userId, Role newRole) {

        log.info("Updateing role for user ID: {} to {}", userId, newRole);

        User user = findById(userId);
        user.setRole(newRole);

        User savedUser = userRepository.save(user);
        log.info("Successfully updated role for user: {} to {}", savedUser.getUsername(), savedUser.getRole());

        return savedUser;

    }

    public User deactivateUser(Long userId) {

        log.info("Deactivating user ID: {}", userId);

        User user = findById(userId);
        user.setIsActive(false);

        User savedUser = userRepository.save(user);
        log.info("Successfully deactivated user: {}", savedUser.getUsername());

        return savedUser;

    }

    public User activateUser(long userId) {

        log.info("Activating user ID: {}", userId);

        User user = findById(userId);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        log.info("Successfully activated user: {}", savedUser.getUsername());

        return savedUser;

    }

    @Transactional(readOnly = true)
    public List<User> findUsersByRole(Role role) {

        log.debug("Finding users with role: {}", role);

        return userRepository.findByRole(role);

    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {

        return userRepository.existsByUsername(username);

    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {

        return userRepository.existsByEmail(email);

    }

}
