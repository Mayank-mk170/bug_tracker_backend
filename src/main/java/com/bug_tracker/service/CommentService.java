
package com.bug_tracker.service;

import com.bug_tracker.dto.CommentRequest;
import com.bug_tracker.dto.CommentResponse;
import com.bug_tracker.model.Comment;
import com.bug_tracker.model.Issue;
import com.bug_tracker.model.User;

import com.bug_tracker.repository.CommentRepository;
import com.bug_tracker.repository.IssueRepository;
import com.bug_tracker.repository.ProjectMemberRepository;
import com.bug_tracker.repository.UserRepository;
import com.bug_tracker.dto.CommentUpdateRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final IssueRepository issueRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    // Add a comment to an issue
    public CommentResponse addComment(
            String issueId,
            CommentRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Issue not found"));

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        boolean isMember =
                projectMemberRepository.existsByProjectIdAndUserId(
                        issue.getProjectId(),
                        user.getId()
                );

        if (!isAdmin && !isMember) {
            throw new IllegalArgumentException(
                    "You are not a member of this project"
            );
        }

        Comment comment = new Comment();

        comment.setIssueId(issueId);
        comment.setUserId(user.getId());
        comment.setUserEmail(user.getEmail());
        comment.setContent(request.getContent().trim());

        LocalDateTime now = LocalDateTime.now();

        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);

        Comment savedComment = commentRepository.save(comment);

        return CommentResponse.from(savedComment);
    }

    // Get all comments for an issue
    public List<CommentResponse> getComments(String issueId) {

        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Issue not found"));

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        boolean isMember =
                projectMemberRepository.existsByProjectIdAndUserId(
                        issue.getProjectId(),
                        user.getId()
                );

        if (!isAdmin && !isMember) {
            throw new IllegalArgumentException(
                    "You are not a member of this project"
            );
        }

        return commentRepository
                .findByIssueIdOrderByCreatedAtAsc(issueId)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    // Update  comment

    public CommentResponse updateComment(
            String commentId,
            CommentUpdateRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Comment not found"));

        if (!comment.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "You can only edit your own comments"
            );
        }

        comment.setContent(request.getContent().trim());
        comment.setUpdatedAt(LocalDateTime.now());

        return CommentResponse.from(commentRepository.save(comment));
    }


    public void deleteComment(String commentId) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Comment not found"));

        if (!comment.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "You can only delete your own comments"
            );
        }

        commentRepository.delete(comment);
    }

}
