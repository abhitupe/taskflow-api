package com.taskflow.api.service;

import com.taskflow.api.exception.BadRequestException;
import com.taskflow.api.exception.ResourceNotFoundException;
import com.taskflow.api.exception.UnauthorizedException;
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

    @Transactional(readOnly = true)
    public Project findByIdWithAccess(Long projectId, Long userId) {

        log.debug("Finding project ID: {} for user ID: {}", projectId, userId);

        Project project = projectRepository.findById(projectId).orElseThrow(() -> {
            log.warn("Project not found with ID: {}", projectId);
            return new ResourceNotFoundException("Project not found with ID: " + projectId);
        });

        if (!hasProjectAccess(project, userId)) {
            log.warn("User {} denied access to project {}", userId, projectId);
            throw new UnauthorizedException("You don't have access to this project");
        }

        return project;

    }

    @Transactional(readOnly = true)
    public Project findById(Long projectId) {

        log.debug("Finding project ID: {}", projectId);

        return projectRepository.findById(projectId).orElseThrow(() -> {
            log.warn("Project not found with ID: {}", projectId);
            return new ResourceNotFoundException("Project not found with ID: " + projectId);
        });

    }

    @Transactional(readOnly = true)
    public List<Project> findUserProjects(Long userId, boolean includeInactive) {

        log.debug("Finding projects for user ID: {}, includeInactive: {}", userId, includeInactive);

        User user = userService.findById(userId);

        if (includeInactive) {
            return projectRepository.findByUser(user);
        } else{
            return projectRepository.findByUserAndIsActive(user, includeInactive);
        }

    }






    private boolean hasProjectAccess(Project project, Long userId) {
        User user = userService.findById(userId);

        if (user.isAdmin() || project.getUser().getId().equals(userId)) {
            return true;
        }

        return false;
    }

}
