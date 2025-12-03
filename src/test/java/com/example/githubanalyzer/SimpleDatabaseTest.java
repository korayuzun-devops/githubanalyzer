package com.example.githubanalyzer;

import com.example.githubanalyzer.client.GitHubClient;
import com.example.githubanalyzer.config.TestConfig;
import com.example.githubanalyzer.entity.ContributorEntity;
import com.example.githubanalyzer.entity.RepoEntity;
import com.example.githubanalyzer.repository.ContributorRepository;
import com.example.githubanalyzer.repository.RepoRepository;
import com.example.githubanalyzer.service.GitHubService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(classes = TestConfig.class)
@ActiveProfiles("test")
@Import(SimpleDatabaseTest.TestGitHubConfig.class)
public class SimpleDatabaseTest {

    @TestConfiguration
    static class TestGitHubConfig {

        @Bean
        public GitHubClient gitHubClient() {
            return org.mockito.Mockito.mock(GitHubClient.class);
        }

        @Bean
        public GitHubService gitHubService(GitHubClient gitHubClient, 
                                          RepoRepository repoRepository, 
                                          ContributorRepository contributorRepository) {
            GitHubService service = new GitHubService();
            // Use reflection to set the autowired fields
            try {
                java.lang.reflect.Field clientField = GitHubService.class.getDeclaredField("gitHubClient");
                clientField.setAccessible(true);
                clientField.set(service, gitHubClient);

                java.lang.reflect.Field repoRepoField = GitHubService.class.getDeclaredField("repoRepository");
                repoRepoField.setAccessible(true);
                repoRepoField.set(service, repoRepository);

                java.lang.reflect.Field contribRepoField = GitHubService.class.getDeclaredField("contributorRepository");
                contribRepoField.setAccessible(true);
                contribRepoField.set(service, contributorRepository);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set fields via reflection", e);
            }
            return service;
        }
    }

    @Autowired
    private RepoRepository repoRepository;

    @Autowired
    private ContributorRepository contributorRepository;

    @Autowired
    private GitHubService gitHubService;

    @Autowired
    private GitHubClient gitHubClient;

    @Test
    public void testSaveAndRetrieveRepo() {
        // Create a test repository
        RepoEntity repo = new RepoEntity();
        repo.setFullName("test/repo");
        repo.setDescription("Test Repository");
        repo.setStars(100);
        repo.setForks(50);
        repo.setUrl("https://github.com/test/repo");
        repo.setWatchersCount(75);
        repo.setLanguage("Java");
        repo.setOpenIssuesCount(10);
        repo.setLicense("MIT");
        repo.setOpenIssues("Open issues: 10");

        // Save the repository
        RepoEntity savedRepo = repoRepository.save(repo);

        // Retrieve the repository from the database
        Optional<RepoEntity> retrievedRepoOptional = repoRepository.findById(savedRepo.getId());

        // Verify the repository was saved and retrieved correctly
        assertTrue(retrievedRepoOptional.isPresent(), "Repository should be found in the database");

        RepoEntity retrievedRepo = retrievedRepoOptional.get();
        assertEquals("test/repo", retrievedRepo.getFullName());
        assertEquals("Test Repository", retrievedRepo.getDescription());
        assertEquals(100, retrievedRepo.getStars());
        assertEquals(50, retrievedRepo.getForks());
        assertEquals("https://github.com/test/repo", retrievedRepo.getUrl());
        assertEquals(75, retrievedRepo.getWatchersCount());
        assertEquals("Java", retrievedRepo.getLanguage());
        assertEquals(10, retrievedRepo.getOpenIssuesCount());
        assertEquals("MIT", retrievedRepo.getLicense());
        assertEquals("Open issues: 10", retrievedRepo.getOpenIssues());
    }

    @Test
    public void testSaveAndRetrieveContributor() {
        // Create a test repository
        RepoEntity repo = new RepoEntity();
        repo.setFullName("test/repo");
        repo.setDescription("Test Repository");
        repo.setStars(100);
        repo.setForks(50);
        repo.setUrl("https://github.com/test/repo");

        // Save the repository
        RepoEntity savedRepo = repoRepository.save(repo);

        // Create a test contributor
        ContributorEntity contributor = new ContributorEntity();
        contributor.setLogin("testuser");
        contributor.setContributions(42);
        contributor.setLocation("Test Location");
        contributor.setCompany("Test Company");
        contributor.setRepo(savedRepo);

        // Save the contributor
        ContributorEntity savedContributor = contributorRepository.save(contributor);

        // Retrieve the contributor from the database
        Optional<ContributorEntity> retrievedContributorOptional = contributorRepository.findById(savedContributor.getId());

        // Verify the contributor was saved and retrieved correctly
        assertTrue(retrievedContributorOptional.isPresent(), "Contributor should be found in the database");

        ContributorEntity retrievedContributor = retrievedContributorOptional.get();
        assertEquals("testuser", retrievedContributor.getLogin());
        assertEquals(42, retrievedContributor.getContributions());
        assertEquals("Test Location", retrievedContributor.getLocation());
        assertEquals("Test Company", retrievedContributor.getCompany());
        assertNotNull(retrievedContributor.getRepo(), "Contributor should be associated with a repository");
        assertEquals(savedRepo.getId(), retrievedContributor.getRepo().getId(), "Contributor should be associated with the correct repository");
    }

    @Test
    public void testRepoContributorsRelationship() {
        // Create a test repository
        RepoEntity repo = new RepoEntity();
        repo.setFullName("test/repo");
        repo.setDescription("Test Repository");
        repo.setStars(100);

        // Save the repository
        RepoEntity savedRepo = repoRepository.save(repo);

        // Create test contributors
        ContributorEntity contributor1 = new ContributorEntity();
        contributor1.setLogin("user1");
        contributor1.setContributions(100);
        contributor1.setLocation("Location 1");
        contributor1.setCompany("Company 1");
        contributor1.setRepo(savedRepo);

        ContributorEntity contributor2 = new ContributorEntity();
        contributor2.setLogin("user2");
        contributor2.setContributions(50);
        contributor2.setLocation("Location 2");
        contributor2.setCompany("Company 2");
        contributor2.setRepo(savedRepo);

        // Save the contributors
        contributorRepository.save(contributor1);
        contributorRepository.save(contributor2);

        // Retrieve all contributors for the repository
        List<ContributorEntity> contributors = contributorRepository.findAll();

        // Verify the contributors were saved correctly
        assertEquals(2, contributors.size(), "There should be 2 contributors in the database");

        // Verify the repository-contributor relationship
        for (ContributorEntity contributor : contributors) {
            assertNotNull(contributor.getRepo(), "Contributor should be associated with a repository");
            assertEquals(savedRepo.getId(), contributor.getRepo().getId(), "Contributor should be associated with the correct repository");
        }
    }

    @Test
    public void testRepoCountIncrease() throws Exception {
        // Count initial repositories
        long initialCount = repoRepository.count();

        // Set up simplified mock responses with only essential fields
        String reposJson = "[" +
            "{\"id\":1,\"name\":\"commons-lang\",\"full_name\":\"apache/commons-lang\"," +
            "\"description\":\"Test Repo 1\",\"html_url\":\"https://github.com/apache/commons-lang\"," +
            "\"stargazers_count\":4500,\"watchers_count\":4500,\"language\":\"Java\"," +
            "\"forks_count\":2000,\"open_issues_count\":120,\"open_issues\":120," +
            "\"license\":{\"name\":\"Apache License 2.0\"}}" +
            ",{\"id\":2,\"name\":\"commons-io\",\"full_name\":\"apache/commons-io\"," +
            "\"description\":\"Test Repo 2\",\"html_url\":\"https://github.com/apache/commons-io\"," +
            "\"stargazers_count\":3500,\"watchers_count\":3500,\"language\":\"Java\"," +
            "\"forks_count\":1500,\"open_issues_count\":80,\"open_issues\":80," +
            "\"license\":{\"name\":\"Apache License 2.0\"}}" +
            "]";

        String contributorsJson = "[{\"login\":\"contributor1\",\"contributions\":100}]";
        String userInfoJson = "{\"login\":\"contributor1\",\"company\":\"Test Company\",\"location\":\"Test Location\"}";

        // Configure mock responses
        when(gitHubClient.getApacheRepos(anyInt())).thenReturn(reposJson);
        when(gitHubClient.getRepoContributors(anyString(), anyInt())).thenReturn(contributorsJson);
        when(gitHubClient.getUserInfo(anyString())).thenReturn(userInfoJson);

        // Call the service method
        List<RepoEntity> savedRepos = gitHubService.fetchAndSaveTopApacheRepos();

        // Verify repos were saved
        assertNotNull(savedRepos);
        assertFalse(savedRepos.isEmpty());

        // Verify the count increased by the expected amount
        long finalCount = repoRepository.count();
        assertEquals(initialCount + savedRepos.size(), finalCount, 
            "Repository count should increase by the number of saved repos");
    }
}
