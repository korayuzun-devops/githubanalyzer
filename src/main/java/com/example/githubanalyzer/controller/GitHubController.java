package com.example.githubanalyzer.controller;

import com.example.githubanalyzer.entity.ContributorEntity;
import com.example.githubanalyzer.entity.RepoEntity;
import com.example.githubanalyzer.service.GitHubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GitHubController {

    private static final Logger logger = LoggerFactory.getLogger(GitHubController.class);

    @Autowired
    private GitHubService githubService;

    /**
     * Fetch and save top Apache repositories and their contributors
     * @return List of repositories
     */
    @PostMapping("/fetch-and-save")
    public ResponseEntity<?> fetchAndSaveTopApacheRepos() {
        try {
            logger.info("Received request to fetch and save top Apache repositories");
            List<RepoEntity> repos = githubService.fetchAndSaveTopApacheRepos();
            logger.info("Successfully fetched and saved {} repositories", repos.size());
            return ResponseEntity.ok(repos);
        } catch (Exception e) {
            logger.error("Error fetching and saving repositories: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Get all repositories from database
     * @return List of repositories
     */
    @GetMapping("/repos")
    public List<RepoEntity> getAllRepos() {
        return githubService.getAllRepos();
    }

    /**
     * Get all contributors from database
     * @return List of contributors
     */
    @GetMapping("/contributors")
    public List<ContributorEntity> getAllContributors() {
        return githubService.getAllContributors();
    }

    /**
     * Get formatted repository and contributor information
     * @return Formatted string with repository and contributor information
     */
    @GetMapping("/formatted-info")
    public String getFormattedInfo() {
        return githubService.getFormattedRepoAndContributorInfo();
    }

    // Legacy endpoints for backward compatibility

    @GetMapping("/top-contributors")
    public Map<String, Integer> getTopContributors() {
        return githubService.getTopContributors();
    }

    @GetMapping("/top-repos")
    public List<Map<String, Object>> getTopRepos() {
        return githubService.fetchTopApacheRepos();
    }
}
