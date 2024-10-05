package com.example.classroom_assignment_management.repository;

import com.example.classroom_assignment_management.entity.Assignment;
import com.example.classroom_assignment_management.entity.ClassLevel;
import com.example.classroom_assignment_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCreatedBy(User teacher);
    List<Assignment> findByClassLevel(ClassLevel classLevel);
}
