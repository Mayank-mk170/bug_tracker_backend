
package com.bug_tracker.repository;

import com.bug_tracker.model.Project;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository
        extends MongoRepository<Project, String> {

    Optional<Project> findByProjectKey(String projectKey);

    boolean existsByProjectKey(String projectKey);

    List<Project> findByCreatedBy(String createdBy);
}
