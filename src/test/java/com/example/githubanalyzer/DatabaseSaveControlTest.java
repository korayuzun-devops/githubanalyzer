package com.example.githubanalyzer;

import com.example.githubanalyzer.entity.ContributorEntity;
import com.example.githubanalyzer.entity.RepoEntity;
import com.example.githubanalyzer.repository.ContributorRepository;
import com.example.githubanalyzer.repository.RepoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class DatabaseSaveControlTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RepoRepository repoRepository;

    @Autowired
    private ContributorRepository contributorRepository;

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
        
        // Flush changes to the database
        entityManager.flush();
        
        // Clear the persistence context to ensure data is retrieved from the database
        entityManager.clear();

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
        repo.setLanguage("Java");
        
        // Save the repository
        RepoEntity savedRepo = entityManager.persistAndFlush(repo);
        
        // Create a test contributor
        ContributorEntity contributor = new ContributorEntity();
        contributor.setLogin("testuser");
        contributor.setContributions(50);
        contributor.setLocation("Test Location");
        contributor.setCompany("Test Company");
        contributor.setRepo(savedRepo);
        
        // Save the contributor
        ContributorEntity savedContributor = contributorRepository.save(contributor);
        
        // Flush changes to the database
        entityManager.flush();
        
        // Clear the persistence context to ensure data is retrieved from the database
        entityManager.clear();
        
        // Retrieve the contributor from the database
        Optional<ContributorEntity> retrievedContributorOptional = contributorRepository.findById(savedContributor.getId());
        
        // Verify the contributor was saved and retrieved correctly
        assertTrue(retrievedContributorOptional.isPresent(), "Contributor should be found in the database");
        
        ContributorEntity retrievedContributor = retrievedContributorOptional.get();
        assertEquals("testuser", retrievedContributor.getLogin());
        assertEquals(50, retrievedContributor.getContributions());
        assertEquals("Test Location", retrievedContributor.getLocation());
        assertEquals("Test Company", retrievedContributor.getCompany());
        assertNotNull(retrievedContributor.getRepo(), "Contributor should have a repository");
        assertEquals("test/repo", retrievedContributor.getRepo().getFullName());
    }

    @Test
    public void testRepoContributorsRelationship() {
        // Create a test repository
        RepoEntity repo = new RepoEntity();
        repo.setFullName("test/repo");
        repo.setDescription("Test Repository");
        repo.setStars(100);
        repo.setLanguage("Java");
        
        // Save the repository
        RepoEntity savedRepo = entityManager.persistAndFlush(repo);
        
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
        
        // Flush changes to the database
        entityManager.flush();
        
        // Clear the persistence context to ensure data is retrieved from the database
        entityManager.clear();
        
        // Retrieve all contributors for the repository
        RepoEntity retrievedRepo = repoRepository.findById(savedRepo.getId()).orElseThrow();
        List<ContributorEntity> contributors = retrievedRepo.getContributors();
        
        // Verify the contributors were saved and retrieved correctly
        assertNotNull(contributors, "Contributors list should not be null");
        assertEquals(2, contributors.size(), "Repository should have 2 contributors");
        
        // Verify the first contributor
        ContributorEntity retrievedContributor1 = contributors.stream()
                .filter(c -> c.getLogin().equals("user1"))
                .findFirst()
                .orElseThrow();
        assertEquals(100, retrievedContributor1.getContributions());
        assertEquals("Location 1", retrievedContributor1.getLocation());
        assertEquals("Company 1", retrievedContributor1.getCompany());
        
        // Verify the second contributor
        ContributorEntity retrievedContributor2 = contributors.stream()
                .filter(c -> c.getLogin().equals("user2"))
                .findFirst()
                .orElseThrow();
        assertEquals(50, retrievedContributor2.getContributions());
        assertEquals("Location 2", retrievedContributor2.getLocation());
        assertEquals("Company 2", retrievedContributor2.getCompany());
    }
}