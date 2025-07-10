package com.taskflow.api.repository;

import com.taskflow.api.model.Task;
import com.taskflow.api.model.User;
import com.taskflow.api.model.Project;
import com.taskflow.api.model.enums.TaskStatus;
import com.taskflow.api.model.enums.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Find tasks by project
    List<Task> findByProject(Project project);
    List<Task> findByProjectId(Long projectId);

    // Find tasks by assignee
    List<Task> findByAssignee(User assignee);
    List<Task> findByAssigneeId(Long assigneeId);

    // Find tasks by status
    List<Task> findByStatus(TaskStatus status);

    // Find tasks by priority
    List<Task> findByPriority(Priority priority);

    // Find overdue tasks
    List<Task> findByDueDateBeforeAndStatusNot(LocalDateTime date, TaskStatus status);

    // Find tasks in progress
    @Query("SELECT t FROM Task t WHERE t.status IN ('IN_PROGRESS', 'IN_REVIEW', 'TESTING')")
    List<Task> findTasksInProgress();

    // Find tasks by project and status
    List<Task> findByProjectAndStatus(Project project, TaskStatus status);

    // Count tasks by status for a project
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    Long countByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") TaskStatus status);
}