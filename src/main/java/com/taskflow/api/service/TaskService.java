package com.taskflow.api.service;

import com.taskflow.api.exception.BadRequestException;
import com.taskflow.api.model.Project;
import com.taskflow.api.model.Task;
import com.taskflow.api.model.User;
import com.taskflow.api.model.enums.Priority;
import com.taskflow.api.model.enums.TaskStatus;
import com.taskflow.api.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final ProjectService projectService;

    public Task createTask(Task task, Long projectId, Long userId) {
        log.info("Creating new task '{}' in project ID: {} by user ID: {}",
                task.getTitle(), projectId, userId);

        // Validate project access
        Project project = projectService.findByIdWithAccess(projectId, userId);

        if (!project.getIsActive()) {
            log.warn("Task creation failed: Project {} is not active", projectId);
            throw new BadRequestException("Cannot create task in inactive project");
        }

        // Set the project
        task.setProject(project);

        // Set default values
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        if (task.getPriority() == null) {
            task.setPriority(Priority.MEDIUM);
        }

        // Validate due date
        if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now())) {
            log.warn("Task creation failed: Due date is in the past");
            throw new BadRequestException("Due date cannot be in the past");
        }

        // Validate assignee if provided
        if (task.getAssignee() != null) {
            User assignee = userService.findById(task.getAssignee().getId());
            if (!assignee.getIsActive()) {
                log.warn("Task creation failed: Assignee {} is not active", task.getAssignee().getId());
                throw new BadRequestException("Cannot assign task to inactive user");
            }
            task.setAssignee(assignee);
        }

        Task savedTask = taskRepository.save(task);
        log.info("Successfully created task '{}' with ID: {} in project '{}'",
                savedTask.getTitle(), savedTask.getId(), project.getName());

        return savedTask;
    }

}
