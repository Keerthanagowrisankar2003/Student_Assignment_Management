package com.example.classroom_assignment_management.controller;

import com.example.classroom_assignment_management.entity.Assignment;
import com.example.classroom_assignment_management.entity.ClassLevel;
import com.example.classroom_assignment_management.entity.Role;
import com.example.classroom_assignment_management.entity.User;
import com.example.classroom_assignment_management.repository.AssignmentRepository;
import com.example.classroom_assignment_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private UserRepository userRepository;

    // Add New Assignment (Teacher Only)
    @PostMapping("/add")
    public ResponseEntity<?> addAssignment(@RequestBody AssignmentRequest assignmentRequest, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> optionalTeacher = userRepository.findByUsername(username);
        if (!optionalTeacher.isPresent()) {
            return ResponseEntity.status(403).body("Access Denied");
        }
        User teacher = optionalTeacher.get();
        if (teacher.getRole() != Role.TEACHER) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        Assignment assignment = new Assignment();
        assignment.setTitle(assignmentRequest.getTitle());
        assignment.setDescription(assignmentRequest.getDescription());
        assignment.setDueDate(assignmentRequest.getDueDate());
        assignment.setClassLevel(assignmentRequest.getClassLevel());
        assignment.setCreatedBy(teacher);
        assignment.setAttachmentUrl(assignmentRequest.getAttachmentUrl()); // Handle file upload as needed

        assignmentRepository.save(assignment);
        return ResponseEntity.ok("Assignment created successfully");
    }

    // View All Assignments Created by Teacher
    @GetMapping("/myAssignment")
    public ResponseEntity<?> getMyAssignments(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> optionalTeacher = userRepository.findByUsername(username);
        if (!optionalTeacher.isPresent()) {
            return ResponseEntity.status(403).body("Access Denied");
        }
        User teacher = optionalTeacher.get();
        if (teacher.getRole() != Role.TEACHER) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        List<Assignment> assignments = assignmentRepository.findByCreatedBy(teacher);
        return ResponseEntity.ok(assignments);
    }

    // Edit Assignment (Teacher Only)
    @PutMapping("/edit/{id}")
    public ResponseEntity<?> editAssignment(@PathVariable Long id, @RequestBody AssignmentRequest assignmentRequest, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> optionalTeacher = userRepository.findByUsername(username);
        if (!optionalTeacher.isPresent()) {
            return ResponseEntity.status(403).body("Access Denied");
        }
        User teacher = optionalTeacher.get();
        if (teacher.getRole() != Role.TEACHER) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        Optional<Assignment> optionalAssignment = assignmentRepository.findById(id);
        if (!optionalAssignment.isPresent()) {
            return ResponseEntity.badRequest().body("Assignment not found");
        }
        Assignment assignment = optionalAssignment.get();

        // Ensure the assignment belongs to the teacher
        if (!assignment.getCreatedBy().getId().equals(teacher.getId())) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        assignment.setTitle(assignmentRequest.getTitle());
        assignment.setDescription(assignmentRequest.getDescription());
        assignment.setDueDate(assignmentRequest.getDueDate());
        assignment.setClassLevel(assignmentRequest.getClassLevel());
        assignment.setAttachmentUrl(assignmentRequest.getAttachmentUrl()); // Handle file upload as needed

        assignmentRepository.save(assignment);
        return ResponseEntity.ok("Assignment updated successfully");
    }

    // Delete Assignment (Teacher Only)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAssignment(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> optionalTeacher = userRepository.findByUsername(username);
        if (!optionalTeacher.isPresent()) {
            return ResponseEntity.status(403).body("Access Denied");
        }
        User teacher = optionalTeacher.get();
        if (teacher.getRole() != Role.TEACHER) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        Optional<Assignment> optionalAssignment = assignmentRepository.findById(id);
        if (!optionalAssignment.isPresent()) {
            return ResponseEntity.badRequest().body("Assignment not found");
        }
        Assignment assignment = optionalAssignment.get();

        // Ensure the assignment belongs to the teacher
        if (!assignment.getCreatedBy().getId().equals(teacher.getId())) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        assignmentRepository.delete(assignment);
        return ResponseEntity.ok("Assignment deleted successfully");
    }

    // View All Available Assignments for Student's Class
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableAssignments(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> optionalStudent = userRepository.findByUsername(username);
        if (!optionalStudent.isPresent()) {
            return ResponseEntity.status(403).body("Access Denied");
        }
        User student = optionalStudent.get();
        if (student.getRole() != Role.STUDENT) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        // Assuming you have a way to determine the student's class level
        // For simplicity, let's assume all students are in 11th or 12th based on an additional field
        // Here, I'll use a placeholder. Adjust as per your actual implementation.

        ClassLevel studentClassLevel = ClassLevel.ELEVENTH; // Replace with actual logic

        List<Assignment> assignments = assignmentRepository.findByClassLevel(studentClassLevel);
        return ResponseEntity.ok(assignments);
    }
}

// DTO
class AssignmentRequest {
    private String title;
    private String description;
    private LocalDate dueDate;
    private String attachmentUrl; // Handle file uploads as needed
    private ClassLevel classLevel;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
    
    public ClassLevel getClassLevel() { return classLevel; }
    public void setClassLevel(ClassLevel classLevel) { this.classLevel = classLevel; }
}
