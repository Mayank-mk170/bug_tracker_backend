
package com.bug_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentUpdateRequest {

    @NotBlank(message = "Comment cannot be empty")
    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String content;
}
