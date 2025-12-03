package com.example.githubanalyzer.service;

import com.example.githubanalyzer.client.GitHubClient;
import com.example.githubanalyzer.dto.RepoWithContributorDTO;
import com.example.githubanalyzer.entity.ContributorEntity;
import com.example.githubanalyzer.entity.RepoEntity;
import com.example.githubanalyzer.repository.ContributorRepository;
import com.example.githubanalyzer.repository.RepoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GitHubService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubService.class);
    private static final int REPOS_TO_FETCH = 100;
    private static final int TOP_REPOS_COUNT = 5;
    private static final int TOP_CONTRIBUTORS_COUNT = 10;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RepoRepository repoRepository;

    @Autowired
    private ContributorRepository contributorRepository;

    @Autowired
    private GitHubClient gitHubClient;

    /**
     * Fetches the top 5 Apache repositories by stargazer count from the 100 most recently updated repos,
     * along with their top 10 contributors, and saves all data to the database.
     * @return List of repository information
     */
    @Transactional
    public List<RepoEntity> fetchAndSaveTopApacheRepos() {
        try {
            logger.info("Fetching {} recently updated Apache repositories", REPOS_TO_FETCH);

            // 1. Fetch recently updated repositories from Apache organization
            String reposResponse = gitHubClient.getApacheRepos(REPOS_TO_FETCH);
            JsonNode repos = objectMapper.readTree(reposResponse);

            // 2. Convert to list and sort by stargazer count
            List<JsonNode> reposList = new ArrayList<>();
            repos.forEach(reposList::add);

            reposList.sort((a, b) -> {
                int bStars = b.has("stargazers_count") && !b.get("stargazers_count").isNull() ? 
                    b.get("stargazers_count").asInt() : 0;
                int aStars = a.has("stargazers_count") && !a.get("stargazers_count").isNull() ? 
                    a.get("stargazers_count").asInt() : 0;
                return Integer.compare(bStars, aStars);
            });

            // 3. Take top 5 repositories
            List<JsonNode> topRepos = reposList.stream()
                .limit(TOP_REPOS_COUNT)
                .collect(Collectors.toList());

            logger.info("Selected top {} repositories by stargazer count", TOP_REPOS_COUNT);

            // 4. Process each repository and save to database
            List<RepoEntity> savedRepos = new ArrayList<>();

            for (JsonNode repo : topRepos) {
                String repoName = repo.has("name") && !repo.get("name").isNull() ? 
                                repo.get("name").asText() : "unknown";
                logger.info("Processing repository: {}", repoName);

                // Create and populate repository entity
                RepoEntity repoEntity = new RepoEntity();
                repoEntity.setFullName(repo.has("full_name") && !repo.get("full_name").isNull() ? 
                                     repo.get("full_name").asText() : repoName);
                repoEntity.setDescription(repo.has("description") && !repo.get("description").isNull() ? 
                                        repo.get("description").asText() : "No description");
                repoEntity.setStars(repo.has("stargazers_count") && !repo.get("stargazers_count").isNull() ? 
                                   repo.get("stargazers_count").asInt() : 0);
                repoEntity.setForks(repo.has("forks_count") && !repo.get("forks_count").isNull() ? 
                                   repo.get("forks_count").asInt() : 0);
                repoEntity.setUrl(repo.has("html_url") && !repo.get("html_url").isNull() ? 
                                repo.get("html_url").asText() : "");
                repoEntity.setWatchersCount(repo.has("watchers_count") && !repo.get("watchers_count").isNull() ? 
                                         repo.get("watchers_count").asInt() : 0);
                repoEntity.setLanguage(repo.has("language") && !repo.get("language").isNull() ? 
                                     repo.get("language").asText() : "Not specified");
                repoEntity.setOpenIssuesCount(repo.has("open_issues_count") && !repo.get("open_issues_count").isNull() ? 
                                           repo.get("open_issues_count").asInt() : 0);

                // Handle license information
                if (repo.has("license") && !repo.get("license").isNull() && 
                    repo.get("license").has("name") && !repo.get("license").get("name").isNull()) {
                    repoEntity.setLicense(repo.get("license").get("name").asText());
                } else {
                    repoEntity.setLicense("No license");
                }

                // Get open issues information
                String openIssues = "Open issues: " + (repo.has("open_issues") && !repo.get("open_issues").isNull() ? 
                                                    repo.get("open_issues").asText() : "0");
                repoEntity.setOpenIssues(openIssues);

                // Save repository to database
                repoEntity = repoRepository.save(repoEntity);

                // 5. Fetch top contributors for this repository
                processContributors(repoName, repoEntity);

                savedRepos.add(repoEntity);
            }

            logger.info("Successfully processed and saved {} repositories with their contributors", savedRepos.size());
            return savedRepos;

        } catch (Exception e) {
            logger.error("Error fetching and processing repositories: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching and processing repositories: " + e.getMessage(), e);
        }
    }

    /**
     * Process contributors for a repository
     * @param repoName Repository name
     * @param repoEntity Repository entity
     */
    private void processContributors(String repoName, RepoEntity repoEntity) throws Exception {
        logger.info("Fetching contributors for repository: {}", repoName);

        String contributorsResponse = gitHubClient.getRepoContributors(repoName, TOP_CONTRIBUTORS_COUNT);
        JsonNode contributors = objectMapper.readTree(contributorsResponse);

        List<ContributorEntity> contributorEntities = new ArrayList<>();

        int count = 0;
        for (JsonNode contributor : contributors) {
            if (count >= TOP_CONTRIBUTORS_COUNT) break;

            String login = contributor.has("login") && !contributor.get("login").isNull() ? 
                        contributor.get("login").asText() : "unknown";
            int contributions = contributor.has("contributions") && !contributor.get("contributions").isNull() ? 
                             contributor.get("contributions").asInt() : 0;

            logger.info("Processing contributor: {} with {} contributions", login, contributions);

            // Get detailed user information
            String userInfoResponse = gitHubClient.getUserInfo(login);
            JsonNode userInfo = objectMapper.readTree(userInfoResponse);

            // Create and populate contributor entity
            ContributorEntity contributorEntity = new ContributorEntity();
            contributorEntity.setLogin(login);
            contributorEntity.setContributions(contributions);
            contributorEntity.setRepo(repoEntity);

            // Set location and company if available
            contributorEntity.setLocation(userInfo.has("location") && !userInfo.get("location").isNull() ? 
                                       userInfo.get("location").asText() : "Not specified");
            contributorEntity.setCompany(userInfo.has("company") && !userInfo.get("company").isNull() ? 
                                      userInfo.get("company").asText() : "Not specified");

            contributorEntities.add(contributorEntity);
            count++;
        }

        // Save all contributors
        contributorRepository.saveAll(contributorEntities);
        logger.info("Saved {} contributors for repository: {}", contributorEntities.size(), repoName);
    }

    /**
     * Get all repositories from database
     * @return List of repositories
     */
    @Transactional(readOnly = true)
    public List<RepoEntity> getAllRepos() {
        return repoRepository.findAll();
    }

    /**
     * Get all contributors from database
     * @return List of contributors
     */
    @Transactional(readOnly = true)
    public List<ContributorEntity> getAllContributors() {
        return contributorRepository.findAll();
    }

    /**
     * Format repository and contributor information for display
     * @return Formatted string with repository and contributor information
     */
    @Transactional(readOnly = true)
    public String getFormattedRepoAndContributorInfo() {
        List<RepoEntity> repos = repoRepository.findAll();
        StringBuilder result = new StringBuilder("Top Apache Repositories and Contributors:\n\n");

        for (RepoEntity repo : repos) {
            result.append("Repository: ").append(repo.getFullName())
                  .append(" (⭐ ").append(repo.getStars()).append(")\n")
                  .append("Language: ").append(repo.getLanguage()).append("\n")
                  .append("Watchers: ").append(repo.getWatchersCount()).append("\n")
                  .append("Open Issues: ").append(repo.getOpenIssuesCount()).append("\n")
                  .append("License: ").append(repo.getLicense()).append("\n")
                  .append("Contributors:\n");

            for (ContributorEntity contributor : repo.getContributors()) {
                result.append("  - ").append(repo.getFullName())
                      .append(" - ").append(contributor.getLogin())
                      .append(", location: ").append(contributor.getLocation())
                      .append(", company: ").append(contributor.getCompany())
                      .append(", contributions: ").append(contributor.getContributions())
                      .append("\n");
            }

            result.append("\n");
        }

        return result.toString();
    }

    // Legacy methods for backward compatibility

    @Transactional(readOnly = true)
    public Map<String, Integer> getTopContributors() {
        Map<String, Integer> contributionsMap = new HashMap<>();
        List<ContributorEntity> contributors = contributorRepository.findAll();

        for (ContributorEntity contributor : contributors) {
            contributionsMap.put(contributor.getLogin(), contributor.getContributions());
        }

        return contributionsMap;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> fetchTopApacheRepos() {
        List<RepoEntity> repos = repoRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (RepoEntity repo : repos) {
            Map<String, Object> repoInfo = new HashMap<>();
            repoInfo.put("name", repo.getFullName());
            repoInfo.put("stars", repo.getStars());
            repoInfo.put("description", repo.getDescription());
            result.add(repoInfo);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public String getFormattedTopApacheRepos() {
        List<Map<String, Object>> topRepos = fetchTopApacheRepos();

        StringBuilder result = new StringBuilder("Top Apache Repositories:\n");
        for (int i = 0; i < topRepos.size(); i++) {
            Map<String, Object> repo = topRepos.get(i);
            result.append(i + 1)
                  .append(". ")
                  .append(repo.get("name"))
                  .append(" (⭐ ")
                  .append(repo.get("stars"))
                  .append("): ")
                  .append(repo.get("description"))
                  .append("\n");
        }

        return result.toString();
    }
}
