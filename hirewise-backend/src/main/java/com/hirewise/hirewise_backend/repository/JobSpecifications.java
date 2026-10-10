package com.hirewise.hirewise_backend.repository;

import com.hirewise.hirewise_backend.entity.EmploymentType;
import com.hirewise.hirewise_backend.entity.Job;
import com.hirewise.hirewise_backend.entity.JobStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class JobSpecifications {

    public static Specification<Job> filterJobs(
            JobStatus status,
            String keyword,
            String location,
            EmploymentType employmentType) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titlePredicate = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descPredicate = cb.like(cb.lower(root.get("description")), pattern);

                Join<Job, String> skillsJoin = root.join("requiredSkills", JoinType.LEFT);
                Predicate skillPredicate = cb.like(cb.lower(skillsJoin), pattern);

                if (query != null) {
                    query.distinct(true);
                }

                predicates.add(cb.or(titlePredicate, descPredicate, skillPredicate));
            }

            if (StringUtils.hasText(location)) {
                String locPattern = "%" + location.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("location")), locPattern));
            }

            if (employmentType != null) {
                predicates.add(cb.equal(root.get("employmentType"), employmentType));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
