
package com.bug_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {

    @NotBlank(message = "Comment cannot be empty")
    @Size(min = 1, max = 1000,
            message = "Comment must not exceed 1000 characters")
    private String content;
}
