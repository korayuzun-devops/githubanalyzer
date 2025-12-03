package com.example.githubanalyzer;

import com.example.githubanalyzer.entity.RepoEntity;
import com.example.githubanalyzer.service.GitHubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class GithubanalyzerApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(GithubanalyzerApplication.class);

    private final GitHubService gitHubService;

    public GithubanalyzerApplication(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    public static void main(String[] args) {
        SpringApplication.run(GithubanalyzerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting GitHub Analyzer application");
        logger.info("Fetching top Apache repositories from GitHub...");

        try {
            // Fetch and save top Apache repositories and their contributors
            List<RepoEntity> topRepos = gitHubService.fetchAndSaveTopApacheRepos();
            logger.info("Successfully fetched and saved {} repositories", topRepos.size());

            // Display formatted repository and contributor information
            String formattedInfo = gitHubService.getFormattedRepoAndContributorInfo();
            System.out.println("\n" + formattedInfo);

            logger.info("GitHub Analyzer completed successfully");
        } catch (Exception e) {
            logger.error("Error running GitHub Analyzer: {}", e.getMessage(), e);
            System.err.println("Error: " + e.getMessage());
        }
    }
}
