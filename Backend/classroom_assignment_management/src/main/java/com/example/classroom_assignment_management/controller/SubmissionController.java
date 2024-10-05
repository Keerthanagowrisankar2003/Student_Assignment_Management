package com.example.classroom_assignment_management.controller;

import com.example.classroom_assignment_management.entity.Assignment;
import com.example.classroom_assignment_management.entity.Submission;
import com.example.classroom_assignment_management.entity.User;
import com.example.classroom_assignment_management.entity.Role;
import com.example.classroom_assignment_management.repository.AssignmentRepository;
import com.example.classroom_assignment_management.repository.SubmissionRepository;
import com.example.classroom_assignment_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private UserRepository userRepository;

    // Submit an Assignment (Student Only)
    @PostMapping("/submit")
    public ResponseEntity<?> submitAssignment(@RequestBody SubmissionRequest submissionRequest, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> optionalStudent = userRepository.findByUsername(username);
        if (!optionalStudent.isPresent()) {
            return ResponseEntity.status(403).body("Access Denied");
        }
        User student = optionalStudent.get();
        if (student.getRole() != Role.STUDENT) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        Optional<Assignment> optionalAssignment = assignmentRepository.findById(submissionRequest.getAssignmentId());
        if (!optionalAssignment.isPresent()) {
            return ResponseEntity.badRequest().body("Invalid Assignment ID");
        }
        Assignment assignment = optionalAssignment.get();

        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setSubmissionText(submissionRequest.getSubmissionText());
        submission.setSubmissionDate(LocalDateTime.now());
        submission.setStatus("Submitted");
        submission.setAttachmentUrl(submissionRequest.getAttachmentUrl()); // Handle file upload as needed

        submissionRepository.save(submission);
        return ResponseEntity.ok("Assignment submitted successfully");
    }

    // View Submitted Assignments by Student
    @GetMapping("/my")
    public ResponseEntity<?> getMySubmissions(Authentication authentication) {
        String username = authentication.getName();
        Optional<User> optionalStudent = userRepository.findByUsername(username);
        if (!optionalStudent.isPresent()) {
            return ResponseEntity.status(403).body("Access Denied");
        }
        User student = optionalStudent.get();
        if (student.getRole() != Role.STUDENT) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        List<Submission> submissions = submissionRepository.findByStudent(student);
        return ResponseEntity.ok(submissions);
    }

    // View All Submissions for an Assignment (Teacher Only)
    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<?> getSubmissionsForAssignment(@PathVariable Long assignmentId, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> optionalTeacher = userRepository.findByUsername(username);
        if (!optionalTeacher.isPresent()) {
            return ResponseEntity.status(403).body("Access Denied");
        }
        User teacher = optionalTeacher.get();
        if (teacher.getRole() != Role.TEACHER) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        Optional<Assignment> optionalAssignment = assignmentRepository.findById(assignmentId);
        if (!optionalAssignment.isPresent()) {
            return ResponseEntity.badRequest().body("Assignment not found");
        }
        Assignment assignment = optionalAssignment.get();

        // Ensure the assignment belongs to the teacher
        if (!assignment.getCreatedBy().getId().equals(teacher.getId())) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        List<Submission> submissions = submissionRepository.findByAssignment(assignment);
        return ResponseEntity.ok(submissions);
    }

    // Update Submission Status (e.g., Graded) (Teacher Only)
    @PutMapping("/update-status/{submissionId}")
    public ResponseEntity<?> updateSubmissionStatus(@PathVariable Long submissionId, @RequestBody StatusUpdateRequest statusUpdateRequest, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> optionalTeacher = userRepository.findByUsername(username);
        if (!optionalTeacher.isPresent()) {
            return ResponseEntity.status(403).body("Access Denied");
        }
        User teacher = optionalTeacher.get();
        if (teacher.getRole() != Role.TEACHER) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        Optional<Submission> optionalSubmission = submissionRepository.findById(submissionId);
        if (!optionalSubmission.isPresent()) {
            return ResponseEntity.badRequest().body("Submission not found");
        }
        Submission submission = optionalSubmission.get();

        // Ensure the assignment belongs to the teacher
        if (!submission.getAssignment().getCreatedBy().getId().equals(teacher.getId())) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        submission.setStatus(statusUpdateRequest.getStatus());
        submissionRepository.save(submission);
        return ResponseEntity.ok("Submission status updated successfully");
    }
}

// DTOs
class SubmissionRequest {
    private Long assignmentId;
    private String submissionText;
    private String attachmentUrl; // Handle file uploads as needed

    // Getters and Setters
    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
    
    public String getSubmissionText() { return submissionText; }
    public void setSubmissionText(String submissionText) { this.submissionText = submissionText; }
    
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String attachmentUrl) { this.attachmentUrl = attachmentUrl; }
}

class StatusUpdateRequest {
    private String status;

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
