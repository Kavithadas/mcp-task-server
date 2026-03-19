package com.mcp.taskserver.repository;

import com.mcp.taskserver.model.Task;
import com.mcp.taskserver.model.Task.TaskStatus;
import com.mcp.taskserver.model.Task.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    long countByStatus(TaskStatus status);

    long countByPriority(Priority priority);

    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status ORDER BY t.status")
    List<Object[]> countByStatusGrouped();

    @Query("SELECT t.priority, COUNT(t) FROM Task t GROUP BY t.priority ORDER BY t.priority")
    List<Object[]> countByPriorityGrouped();

    @Query("SELECT t.category, COUNT(t) FROM Task t GROUP BY t.category ORDER BY COUNT(t) DESC")
    List<Object[]> countByCategoryGrouped();

    @Query("SELECT t.assignedTo, COUNT(t) FROM Task t WHERE t.assignedTo IS NOT NULL GROUP BY t.assignedTo ORDER BY COUNT(t) DESC")
    List<Object[]> countByAssigneeGrouped();
}
