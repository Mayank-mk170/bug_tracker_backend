
package com.bug_tracker.controller;

import com.bug_tracker.dto.CommentRequest;
import com.bug_tracker.dto.CommentResponse;
import com.bug_tracker.dto.CommentUpdateRequest;
import com.bug_tracker.service.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/issues/{issueId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // Add comment
    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable String issueId,
            @Valid @RequestBody CommentRequest request) {

        CommentResponse response =
                commentService.addComment(issueId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get all comments
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable String issueId) {

        return ResponseEntity.ok(
                commentService.getComments(issueId)
        );
    }


    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable String commentId,
            @Valid @RequestBody CommentUpdateRequest request) {

        CommentResponse response =
                commentService.updateComment(commentId, request);

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String commentId) {

        commentService.deleteComment(commentId);

        return ResponseEntity.noContent().build();
    }

}
