package com.taskflow.api.service;

import com.taskflow.api.exception.BadRequestException;
import com.taskflow.api.model.Project;
import com.taskflow.api.model.User;
import com.taskflow.api.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;

    public Project createProject(Project project, Long userId) {

        log.info("Creating new project '{}' for user ID: {}", project.getName(), userId);

        User owner = userService.findById(userId);
        if (!owner.getIsActive()) {
            log.warn("Project creation failed: User{} is not active", userId);
            throw new BadRequestException("Cannot create project for inactive user");
        }

        project.setUser(owner);

        if (project.getIsActive() == null){
            project.setIsActive(true);
        }

        List<Project> existingProjects = projectRepository.findByUserAndIsActive(owner, true);
        boolean nameExists = existingProjects.stream().anyMatch(p -> p.getName().equalsIgnoreCase(project.getName()));

        if (nameExists) {

            log.warn("Project creation failed: Project name '{}' for already exists for user {}", project.getName(), userId);
            throw new BadRequestException("Project name already exists: " + project.getName());

        }

        Project savedProject = projectRepository.save(project);
        log.info("Successfully created project '{}' with ID: {} for user: {}", savedProject.getName(), savedProject.getId(), owner.getUsername());

        return savedProject;

    }

}
