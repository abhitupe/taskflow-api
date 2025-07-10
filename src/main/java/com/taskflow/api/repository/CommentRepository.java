package com.taskflow.api.repository;

import com.taskflow.api.model.Comment;
import com.taskflow.api.model.Task;
import com.taskflow.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Find comments by task
    List<Comment> findByTask(Task task);
    List<Comment> findByTaskId(Long taskId);

    // Find comments by author
    List<Comment> findByAuthor(User author);
    List<Comment> findByAuthorId(Long authorId);

    // Find comments ordered by creation date
    List<Comment> findByTaskOrderByCreatedAtDesc(Task task);

    // Find recent comments
    List<Comment> findByCreatedAtAfter(LocalDateTime date);

    // Find edited comments
    List<Comment> findByIsEditedTrue();

    // Custom query to find comments with author info
    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.task.id = :taskId ORDER BY c.createdAt DESC")
    List<Comment> findCommentsByTaskWithAuthor(@Param("taskId") Long taskId);
}