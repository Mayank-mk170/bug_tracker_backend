
package com.bug_tracker.repository;

import com.bug_tracker.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {

    List<Comment> findByIssueIdOrderByCreatedAtAsc(String issueId);

    List<Comment> findByUserId(String userId);
}
