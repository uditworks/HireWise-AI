package com.hirewise.hirewise_backend.dto;

import com.hirewise.hirewise_backend.entity.EmploymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

public class JobUpdateRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 10000, message = "Description must be between 10 and 10000 characters")
    private String description;

    @NotBlank(message = "Location is required")
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;

    @Size(max = 80, message = "Salary range must not exceed 80 characters")
    private String salaryRange;

    @NotEmpty(message = "At least one required skill must be specified")
    private Set<@NotBlank(message = "Skill name cannot be blank") String> requiredSkills = new HashSet<>();

    public JobUpdateRequest() {
    }

    public JobUpdateRequest(String title, String description, String location,
                            EmploymentType employmentType, String salaryRange,
                            Set<String> requiredSkills) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.employmentType = employmentType;
        this.salaryRange = salaryRange;
        this.requiredSkills = requiredSkills != null ? requiredSkills : new HashSet<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public EmploymentType getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(EmploymentType employmentType) {
        this.employmentType = employmentType;
    }

    public String getSalaryRange() {
        return salaryRange;
    }

    public void setSalaryRange(String salaryRange) {
        this.salaryRange = salaryRange;
    }

    public Set<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(Set<String> requiredSkills) {
        this.requiredSkills = requiredSkills != null ? requiredSkills : new HashSet<>();
    }
}
