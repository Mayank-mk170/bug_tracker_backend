
package com.bug_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class IssueStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(
            regexp = "TODO|IN_PROGRESS|DONE",
            message = "Status must be TODO, IN_PROGRESS or DONE"
    )
    private String status;
}
