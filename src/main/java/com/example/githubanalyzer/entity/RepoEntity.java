package com.example.githubanalyzer.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class RepoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String description;
    private int stars;
    private int forks;
    private String url;
    @Column(nullable = true)
    private Integer watchersCount = 0;

    private String language;

    @Column(nullable = true)
    private Integer openIssuesCount = 0;

    private String license;

    @Column(length = 1000)
    private String openIssues;

    @OneToMany(mappedBy = "repo", cascade = CascadeType.ALL)
    private List<ContributorEntity> contributors;

    // Manual getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public int getForks() {
        return forks;
    }

    public void setForks(int forks) {
        this.forks = forks;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<ContributorEntity> getContributors() {
        return contributors;
    }

    public void setContributors(List<ContributorEntity> contributors) {
        this.contributors = contributors;
    }

    public Integer getWatchersCount() {
        return watchersCount;
    }

    public void setWatchersCount(Integer watchersCount) {
        this.watchersCount = watchersCount;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getOpenIssuesCount() {
        return openIssuesCount;
    }

    public void setOpenIssuesCount(Integer openIssuesCount) {
        this.openIssuesCount = openIssuesCount;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getOpenIssues() {
        return openIssues;
    }

    public void setOpenIssues(String openIssues) {
        this.openIssues = openIssues;
    }
}
