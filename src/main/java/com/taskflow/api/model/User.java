package com.taskflow.api.model;

import com.taskflow.api.model.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User entity representing users in the task management system
 *
 * Key Relationships:
 * - OneToMany with Project (user owns many projects)
 * - OneToMany with Task (user is assigned to many tasks)
 * - OneToMany with Comment (user authors many comments)
 *
 * Security Features:
 * - Password encryption (handled in service layer)
 * - Role-based access control
 * - Account activation status
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "username")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    /**
     * User role for authorization
     * Stored as string for readability in database
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Role role = Role.DEVELOPER;

    /**
     * Account activation status
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Projects owned by this user
     * OneToMany: One user can own many projects
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Project> ownedProjects = new ArrayList<>();

    /**
     * Tasks assigned to this user
     * OneToMany: One user can be assigned to many tasks
     */
    @OneToMany(mappedBy = "assignee", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Task> assignedTasks = new ArrayList<>();

    /**
     * Comments authored by this user
     * OneToMany: One user can author many comments
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Helper method to add a project to this user
     * Maintains bidirectional relationship consistency
     */
    public void addProject(Project project) {
        ownedProjects.add(project);
        project.setUser(this);
    }

    /**
     * Helper method to remove a project from this user
     * Maintains bidirectional relationship consistency
     */
    public void removeProject(Project project) {
        ownedProjects.remove(project);
        project.setUser(null);
    }

    /**
     * Helper method to add a task assignment to this user
     * Maintains bidirectional relationship consistency
     */
    public void addAssignedTask(Task task) {
        assignedTasks.add(task);
        task.setAssignee(this);
    }

    /**
     * Helper method to remove a task assignment from this user
     * Maintains bidirectional relationship consistency
     */
    public void removeAssignedTask(Task task) {
        assignedTasks.remove(task);
        task.setAssignee(null);
    }

    /**
     * Helper method to add a comment authored by this user
     * Maintains bidirectional relationship consistency
     */
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setAuthor(this);
    }

    /**
     * Helper method to remove a comment authored by this user
     * Maintains bidirectional relationship consistency
     */
    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setAuthor(null);
    }

    /**
     * Business method to get full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Business method to check if user is admin
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /**
     * Business method to check if user is project manager
     */
    public boolean isProjectManager() {
        return role == Role.PROJECT_MANAGER;
    }

    /**
     * toString method excluding relationships to avoid circular references
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}