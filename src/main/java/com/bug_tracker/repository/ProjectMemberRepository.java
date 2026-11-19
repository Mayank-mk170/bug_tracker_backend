
package com.bug_tracker.repository;

import com.bug_tracker.model.ProjectMember;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository
        extends MongoRepository<ProjectMember, String> {

    List<ProjectMember> findByProjectId(String projectId);

    List<ProjectMember> findByUserId(String userId);

    Optional<ProjectMember> findByProjectIdAndUserId(
            String projectId,
            String userId
    );

    boolean existsByProjectIdAndUserId(
            String projectId,
            String userId
    );

    void deleteByProjectIdAndUserId(
            String projectId,
            String userId
    );
}
