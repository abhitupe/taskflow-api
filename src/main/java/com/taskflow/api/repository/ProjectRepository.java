package com.taskflow.api.repository;

import com.taskflow.api.model.Project;
import com.taskflow.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Find projects by owner
    List<Project> findByUser(User user);
    List<Project> findByUserId(Long userId);

    // Find active projects
    List<Project> findByIsActiveTrue();

    // Find projects by owner and status
    List<Project> findByUserAndIsActive(User user, Boolean isActive);

    // Find projects created after a certain date
    List<Project> findByCreatedAtAfter(LocalDateTime date);

    // Custom query to find projects with task count
    @Query("SELECT p FROM Project p LEFT JOIN p.tasks t WHERE p.user.id = :userId GROUP BY p.id")
    List<Project> findProjectsByUserWithTasks(@Param("userId") Long userId);
}