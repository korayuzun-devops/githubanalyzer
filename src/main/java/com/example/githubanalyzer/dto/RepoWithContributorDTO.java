package com.example.githubanalyzer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RepoWithContributorDTO {
    private Long id;
    private String fullName;
    private String description;
    private int stars;
    private int forks;
    private String url;
    private String contributorLogin;
    private int contributions;
}