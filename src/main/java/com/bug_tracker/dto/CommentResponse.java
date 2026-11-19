
package com.bug_tracker.dto;

import com.bug_tracker.model.Comment;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponse {

    private String id;
    private String issueId;
    private String userId;
    private String userEmail;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentResponse from(Comment comment) {

        CommentResponse response = new CommentResponse();

        response.setId(comment.getId());
        response.setIssueId(comment.getIssueId());
        response.setUserId(comment.getUserId());
        response.setUserEmail(comment.getUserEmail());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());

        return response;
    }
}
