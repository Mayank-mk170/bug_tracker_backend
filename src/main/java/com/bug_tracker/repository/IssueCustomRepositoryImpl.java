
package com.bug_tracker.repository;

import com.bug_tracker.model.Issue;

import lombok.RequiredArgsConstructor;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class IssueCustomRepositoryImpl
        implements IssueCustomRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Issue> filterIssues(
            String projectId,
            String status,
            String priority,
            String assigneeEmail) {

        List<Criteria> criteriaList = new ArrayList<>();

        criteriaList.add(
                Criteria.where("projectId").is(projectId)
        );

        if (status != null && !status.isBlank()) {
            criteriaList.add(
                    Criteria.where("status").is(status)
            );
        }

        if (priority != null && !priority.isBlank()) {
            criteriaList.add(
                    Criteria.where("priority").is(priority)
            );
        }

        if (assigneeEmail != null && !assigneeEmail.isBlank()) {
            criteriaList.add(
                    Criteria.where("assigneeEmail").is(assigneeEmail)
            );
        }

        Query query = new Query();

        query.addCriteria(
                new Criteria().andOperator(
                        criteriaList.toArray(new Criteria[0])
                )
        );

        query.with(
                org.springframework.data.domain.Sort
                        .by(org.springframework.data.domain.Sort.Direction.DESC,
                                "createdAt")
        );

        return mongoTemplate.find(query, Issue.class);
    }
}
