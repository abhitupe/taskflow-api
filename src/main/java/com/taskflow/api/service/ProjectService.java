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

import java.awt.event.WindowFocusListener;
import java.time.LocalDateTime;
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

    @Transactional(readOnly = true)
    public List<Project> findAllActiveProjects() {
        log.debug("Finding all active projects");
        return projectRepository.findByIsActiveTrue();
    }

    @Transactional(readOnly = true)
    public List<Project> findAllProjects() {
        log.debug("Finding all projects");
        return projectRepository.findAll();
    }

    public Project updateProject(Long projectId, Project updatedProject, Long userId) {
        log.info("Updating project ID: {} by user ID: {}", projectId, userId);

        Project existingProject = findByIdWithAccess(projectId, userId);

        if (!existingProject.getName().equals(updatedProject.getName())) {
            List<Project> userProjects = projectRepository.findByUserAndIsActive(existingProject.getUser(), true);
            boolean nameExists = userProjects.stream().anyMatch(p -> !p.getId().equals(projectId) &&
                    p.getName().equalsIgnoreCase(updatedProject.getName()));

            if (nameExists) {
                log.warn("Project update failed: Project name '{}' already exists for user {}", existingProject.getName(), userId);
                throw new BadRequestException("Project name already exists: " + updatedProject.getName());
            }

        }

        existingProject.setName(updatedProject.getName());
        existingProject.setDescription(updatedProject.getDescription());

        Project savedProject = projectRepository.save(existingProject);
        log.info("Successfully updated project '{}' with ID: {}", savedProject.getName(), savedProject.getId());

        return savedProject;

    }

    public Project deactivateproject(Long projectId, Long userId) {

        log.info("Deactivating project ID: {} by user ID: {}", projectId, userId);

        Project project = findByIdWithAccess(projectId, userId);
        project.setIsActive(false);

        Project savedProject = projectRepository.save(project);
        log.info("Successfully deactivated project '{}' with ID: {}", savedProject.getName(), savedProject.getId());

        return savedProject;

    }

    public Project reactivateProject(Long projectId, Long userId) {

        log.info("Reactivating project ID: '{}' with user ID: {}", projectId, userId);

        Project project = findByIdWithAccess(projectId, userId);
        project.setIsActive(true);

        Project savedProject = projectRepository.save(project);
        log.info("Successfully reactivate project '{}' with ID: {}", savedProject.getName(), savedProject.getId());

        return savedProject;

    }

    public void deleteProject(Long projectId) {

        log.warn("Permanently deleting project ID: {}", projectId);

        Project project = findById(projectId);

        projectRepository.delete(project);
        log.warn("Successfully deleted project: '{}' with ID: {}", project.getName(), project.getId());

    }

    @Transactional(readOnly = true)
    public List<Project> findProjectsCreatedAfter(LocalDateTime date) {

        log.debug("Finding projects created after: {}", date);
        return projectRepository.findByCreatedAtAfter(date);

    }

    @Transactional(readOnly = true)
    public List<Project> findProjectsWithTasks(Long userId) {

        log.debug("Finding projects with tasks for user ID: {}", userId);
        return projectRepository.findProjectsByUserWithTasks(userId);

    }

    public Project transferOwnership(Long projectId, Long newOwnerId) {

        log.info("Transfering ownership of project ID: {} to user ID: {}", projectId, newOwnerId);

        Project project = findById(projectId);
        User newOwner = userService.findById(newOwnerId);

        if (!newOwner.getIsActive()) {
            log.warn("Ownership transfer failed: New owner {} is not active", newOwnerId);
            throw new BadRequestException("Cannot transfer ownership to inactive user");
        }

        User previousOwner = project.getUser();
        project.setUser(newOwner);

        Project savedProject = projectRepository.save(project);
        log.info("Successfully transferred ownership of project '{}' from {} to {}", savedProject.getName(), previousOwner.getUsername(), newOwner.getUsername());

        return savedProject;

    }

    private boolean hasProjectAccess(Project project, Long userId) {
        User user = userService.findById(userId);

        if (user.isAdmin() || project.getUser().getId().equals(userId)) {
            return true;
        }

        return false;
    }

    public boolean isProjectAccessible(Project project, Long userId) {

        User user = userService.findById(userId);

        if (user.isAdmin()) {
            return true;
        }

        return project.getIsActive() && project.getUser().getId().equals(userId);

    }

    @Transactional(readOnly = true)
    public Project getProjectWithStats(Long projectId, Long userId) {

        log.debug("Getting project stats for project ID: {} by user ID: {}", projectId, userId);

        return findByIdWithAccess(projectId, userId);

    }

}
